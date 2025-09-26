package com.service;

import com.alibaba.fastjson.JSON;
import com.entity.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayerService{

    // 方向枚举
    private static final String[] DIRECTIONS = {"U", "D", "L", "R"};
    private static final String NO_MOVE = "N";

    // 游戏常量
    private static final int MAP_WIDTH = 28;
    private static final int MAP_HEIGHT = 16;
    private static final int TICKS_PER_SECOND = 10;
    private static final int BOMB_EXPLODE_TICKS = 20; // 2秒 = 20ticks

    public CommandResponse makeDecision(GameState gameState) {
        PlayerStateVo myPlayer = gameState.my_player;

        // System.out.println(JSON.toJSONString(gameState));
        // 如果被炸晕，不进行任何操作
        if ("D".equals(myPlayer.status)) {
            return new CommandResponse(NO_MOVE, false);
        }

        // 获取当前位置
        Position myPos = myPlayer.getGridPosition();

        // 策略优先级：
        // 1. 躲避即将爆炸的炸弹
        // 2. 拾取附近道具
        // 3. 占领未占领格子
        // 4. 攻击敌方糖豆人

        // 1. 检查安全情况
        String safeDirection = avoidBombs(gameState, myPos);
        if (safeDirection != null && !safeDirection.equals(NO_MOVE)) {
            return new CommandResponse(safeDirection, false);
        }

        // 2. 寻找附近道具
        String itemDirection = goForItems(gameState, myPos);
        if (itemDirection != null && !itemDirection.equals(NO_MOVE)) {
            return new CommandResponse(itemDirection,
                    shouldPlaceBombForItem(gameState, myPos),
                    calculateStride(myPlayer));
        }

        // 3. 寻找未占领格子
        String territoryDirection = expandTerritory(gameState, myPos);
        if (territoryDirection != null && !territoryDirection.equals(NO_MOVE)) {
            boolean placeBomb = shouldPlaceBomb(gameState, myPos);
            return new CommandResponse(territoryDirection, placeBomb, calculateStride(myPlayer));
        }

        // 4. 攻击敌方
        String attackDirection = attackEnemy(gameState, myPos);
        if (attackDirection != null && !attackDirection.equals(NO_MOVE)) {
            return new CommandResponse(attackDirection, true, calculateStride(myPlayer));
        }

        // 默认行为：随机移动，偶尔放置炸弹
        return getDefaultAction(gameState, myPos);
    }

    // 躲避炸弹策略
    private String avoidBombs(GameState gameState, Position myPos) {
        List<Bomb> dangerousBombs = gameState.bombs.stream()
                .filter(bomb -> isBombDangerous(bomb, myPos, gameState.current_tick))
                .collect(Collectors.toList());

        if (dangerousBombs.isEmpty()) {
            return null;
        }

        // 计算安全方向
        return findSafeDirection(gameState, myPos, dangerousBombs);
    }

    private boolean isBombDangerous(Bomb bomb, Position myPos, int currentTick) {
        if (bomb.explode_at - currentTick > 5) { // 5ticks内爆炸的才危险
            return false;
        }

        // 检查是否在爆炸范围内
        int dx = Math.abs(bomb.position.x - myPos.x);
        int dy = Math.abs(bomb.position.y - myPos.y);

        // 十字爆炸范围
        return (dx <= bomb.range && dy == 0) || (dy <= bomb.range && dx == 0);
    }

    private String findSafeDirection(GameState gameState, Position myPos, List<Bomb> dangerousBombs) {
        // 评估各个方向的安全性
        Map<String, Double> directionSafety = new HashMap<>();

        for (String dir : DIRECTIONS) {
            Position newPos = movePosition(myPos, dir);
            if (isValidPosition(newPos) && !isBlocked(gameState, newPos)) {
                double safetyScore = calculateSafetyScore(gameState, newPos, dangerousBombs);
                directionSafety.put(dir, safetyScore);
            }
        }

        // 选择最安全的方向
        return directionSafety.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(NO_MOVE);
    }

    private double calculateSafetyScore(GameState gameState, Position pos, List<Bomb> dangerousBombs) {
        double score = 1.0;

        // 远离危险炸弹
        for (Bomb bomb : dangerousBombs) {
            double distance = pos.distanceTo(bomb.position);
            score += distance; // 距离越远越安全
        }

        // 偏好未占领格子
        Cell cell = getCell(gameState.map, pos);
        if ("N".equals(cell.ownership)) {
            score += 2.0;
        }

        // 避开敌方糖豆人
        boolean hasEnemyNearby = gameState.other_players.stream()
                .filter(p -> !p.team.equals(gameState.my_player.team))
                .anyMatch(p -> p.getGridPosition().distanceTo(pos) <= 2);
        if (hasEnemyNearby) {
            score -= 1.0;
        }

        return score;
    }

    // 拾取道具策略
    private String goForItems(GameState gameState, Position myPos) {
        if (gameState.map_items.isEmpty()) {
            return null;
        }
        // 考虑道具满了的情况

        // 寻找最近的道具
        MapItem nearestItem = null;
        double minDistance = Double.MAX_VALUE;

        for (MapItem item : gameState.map_items) {
            double distance = myPos.distanceTo(item.position);
            if (distance < minDistance && distance <= 5) { // 只考虑5格内的道具
                minDistance = distance;
                nearestItem = item;
            }
        }

        if (nearestItem == null) {
            return null;
        }

        // 计算朝向道具的方向
        return calculateDirectionToTarget(myPos, nearestItem.position);
    }

    private boolean shouldPlaceBombForItem(GameState gameState, Position myPos) {
        // 如果道具被障碍物阻挡，考虑放置炸弹
        MapItem nearestItem = gameState.map_items.stream()
                .min(Comparator.comparingDouble(item -> myPos.distanceTo(item.position)))
                .orElse(null);

        if (nearestItem == null) return false;

        // 检查路径上是否有可破坏障碍物
        return hasDestructibleObstacleOnPath(gameState, myPos, nearestItem.position);
    }

    // 扩张领土策略
    private String expandTerritory(GameState gameState, Position myPos) {
        // 寻找最近的未占领格子或敌方格子
        Position target = findNearestUnclaimedOrEnemyTerritory(gameState, myPos);

        if (target == null) {
            return null;
        }

        return calculateDirectionToTarget(myPos, target);
    }

    private Position findNearestUnclaimedOrEnemyTerritory(GameState gameState, Position myPos) {
        Position nearest = null;
        double minDistance = Double.MAX_VALUE;

        // 搜索周围一定范围内的格子
        int searchRadius = 8;

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dy = -searchRadius; dy <= searchRadius; dy++) {
                int x = myPos.x + dx;
                int y = myPos.y + dy;

                if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                    Cell cell = getCell(gameState.map, new Position(x, y));
                    double distance = myPos.distanceTo(new Position(x, y));

                    // 优先未占领，其次敌方占领
                    if ("N".equals(cell.ownership) ||
                            (!cell.ownership.equals(gameState.my_player.team) &&
                                    !"N".equals(cell.ownership))) {
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearest = new Position(x, y);
                        }
                    }
                }
            }
        }

        return nearest;
    }

    private boolean shouldPlaceBomb(GameState gameState, Position myPos) {
        // 在以下情况放置炸弹：
        // 1. 附近有敌方糖豆人
        // 2. 附近有可破坏障碍物
        // 3. 随机概率（避免频繁放置）

        boolean nearEnemy = gameState.other_players.stream()
                .filter(p -> !p.team.equals(gameState.my_player.team))
                .anyMatch(p -> p.getGridPosition().distanceTo(myPos) <= 2);

        boolean nearDestructible = hasNearbyDestructibleObstacle(gameState, myPos);
        boolean randomChance = Math.random() < 0.3; // 30%概率随机放置

        return nearEnemy || nearDestructible || randomChance;
    }

    // 攻击敌方策略
    private String attackEnemy(GameState gameState, Position myPos) {
        // 寻找最近的敌方糖豆人
        PlayerStateVo nearestEnemy = gameState.other_players.stream()
                .filter(p -> !p.team.equals(gameState.my_player.team))
                .min(Comparator.comparingDouble(p -> myPos.distanceTo(p.getGridPosition())))
                .orElse(null);

        if (nearestEnemy == null ||
                myPos.distanceTo(nearestEnemy.getGridPosition()) > 5) {
            return null;
        }

        return calculateDirectionToTarget(myPos, nearestEnemy.getGridPosition());
    }

    // 工具方法
    private Position movePosition(Position pos, String direction) {
        switch (direction) {
            case "U": return new Position(pos.x, pos.y + 1);
            case "D": return new Position(pos.x, pos.y - 1);
            case "L": return new Position(pos.x - 1, pos.y);
            case "R": return new Position(pos.x + 1, pos.y);
            default: return pos;
        }
    }

    private boolean isValidPosition(Position pos) {
        return pos.x >= 0 && pos.x < MAP_WIDTH && pos.y >= 0 && pos.y < MAP_HEIGHT;
    }

    private boolean isBlocked(GameState gameState, Position pos) {
        Cell cell = getCell(gameState.map, pos);
        return "I".equals(cell.terrain) || "N".equals(cell.terrain) ||
                hasBombAtPosition(gameState, pos);
    }

    private boolean hasBombAtPosition(GameState gameState, Position pos) {
        return gameState.bombs.stream()
                .anyMatch(bomb -> bomb.position.x == pos.x && bomb.position.y == pos.y);
    }

    private Cell getCell(List<List<Cell>> map, Position pos) {
        if (pos.y >= 0 && pos.y < map.size() &&
                pos.x >= 0 && pos.x < map.get(pos.y).size()) {
            return map.get(pos.y).get(pos.x);
        }
        return null;
    }

    private String calculateDirectionToTarget(Position from, Position to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? "R" : "L";
        } else {
            return dy > 0 ? "U" : "D";
        }
    }

    private boolean hasDestructibleObstacleOnPath(GameState gameState, Position from, Position to) {
        // 简单的直线路径检查
        int steps = Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y));

        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(from.x + t * (to.x - from.x));
            int y = (int) Math.round(from.y + t * (to.y - from.y));

            Position checkPos = new Position(x, y);
            Cell cell = getCell(gameState.map, checkPos);
            if ("D".equals(cell.terrain)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasNearbyDestructibleObstacle(GameState gameState, Position myPos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int x = myPos.x + dx;
                int y = myPos.y + dy;

                if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                    Cell cell = getCell(gameState.map, new Position(x, y));
                    if ("D".equals(cell.terrain)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Integer calculateStride(PlayerStateVo player) {
        // 根据速度计算合适的步长
        double speed = player.getMoveSpeed();
        int pixelsPerTick = (int) (speed * 25); // 1格=50像素，2格/秒=100像素/秒=10像素/tick

        // 可以微调步长进行精细控制
        return Math.max(1, Math.min(14, pixelsPerTick - 1));
    }

    private CommandResponse getDefaultAction(GameState gameState, Position myPos) {
        // 随机移动，小概率放置炸弹
        String randomDir = DIRECTIONS[(int) (Math.random() * DIRECTIONS.length)];
        boolean placeBomb = Math.random() < 0.1; // 10%概率放置炸弹

        return new CommandResponse(randomDir, placeBomb, calculateStride(gameState.my_player));
    }
}
