package com.ttjj.utils;

import java.util.*;

public class AStarAlgorithmUtil {
    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int SPEED_G = 10;
    private static final int SPEED_M = 5;
    private static final int GRID_SIZE = 50;

    private static final char CH = 'C';
    private static final char M = 'M';
    private static final char G = 'G';
    private static final char SH = 'S';
    private static final char FH = 'F';
    private static final char T = 'T';
    private static final char R = 'R';
    private static final char PH = 'P';
    private static final char OH = 'O';
    private static final char W = 'W';
    private static final char RH = 'H';

    // 创建一个映射关系，将字符串映射到字符常量
    private static final Map<String, Character> stringToCharMap = new HashMap<>();

    static {
        stringToCharMap.put("CH", CH);
        stringToCharMap.put("M", M);
        stringToCharMap.put("G", G);
        stringToCharMap.put("SH", SH);
        stringToCharMap.put("FH", FH);
        stringToCharMap.put("T", T);
        stringToCharMap.put("R", R);
        stringToCharMap.put("PH", PH);
        stringToCharMap.put("OH", OH);
        stringToCharMap.put("W", W);
        stringToCharMap.put("RH", RH);
    }

    private static boolean hasType3Item = false;

    public static void setHasType3Item(boolean hasItem) {
        hasType3Item = hasItem;
    }

    private static boolean isWalkable(char terrain) {
        if (hasType3Item) {
            // 在有type=3道具时，W和G也可行走
            return terrain == G || terrain == M || terrain == W || terrain == R || terrain == T;
        } else {
            return terrain == G || terrain == M;
        }
    }

    private static double getTravelCost(char terrain) {
        if (terrain == G) return 10.0 / SPEED_G;
        if (terrain == M) return 10.0 / SPEED_M;
        if (hasType3Item) {
            if (terrain == W || terrain == R || terrain == T) return 10.0 / SPEED_G;
        }
        return Double.MAX_VALUE;
    }

    private static double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public static List<int[]> aStarSearchPoint(char[][] grid, int startX, int startY, int endX, int endY, Set<int[]> otherUserPositions) {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[grid.length][grid[0].length];
        openList.add(new Node(startX, startY, 0, heuristic(startX, startY, endX, endY), null));

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            if (current.x == endX && current.y == endY) {
                return constructPathPoint(current);
            }

            closedList[current.x][current.y] = true;

            for (int[] direction : DIRECTIONS) {
                int neighborX = current.x + direction[0];
                int neighborY = current.y + direction[1];

                if (neighborX >= 0 && neighborX < grid.length && neighborY >= 0 && neighborY < grid[0].length) {
                    // 其他玩家视为障碍物
                    if (isOtherUserPosition(neighborX, neighborY, otherUserPositions)) {
                        continue;
                    }
                    if (closedList[neighborX][neighborY] || !isWalkable(grid[neighborX][neighborY])) continue;

                    double tentativeGCost = current.gCost + getTravelCost(grid[neighborX][neighborY]);
                    Node neighbor = new Node(neighborX, neighborY, tentativeGCost, heuristic(neighborX, neighborY, endX, endY), current);

                    if (openList.stream().noneMatch(n -> n.x == neighborX && n.y == neighborY && n.getFCost() <= neighbor.getFCost())) {
                        openList.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isOtherUserPosition(int x, int y, Set<int[]> otherUserPositions) {
        for (int[] pos : otherUserPositions) {
            if (pos[0] == x && pos[1] == y) {
                return true;
            }
        }
        return false;
    }

    private static List<int[]> constructPathPoint(Node node) {
        List<int[]> path = new ArrayList<>();
        Node current = node;

        while (current.parent != null) {
            path.add(new int[]{current.x, current.y});
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static int[] convertToGridCoordinate(int x, int y) {
        return new int[]{x / GRID_SIZE, y / GRID_SIZE};
    }

    // 将二维字符串数组转换为二维字符数组
    public static char[][] listToCharArray(List<List<String>> lists) {
        int rows = lists.size();
        int cols = lists.get(0).size();
        char[][] charArray = new char[rows][cols];
        for (int i = 0; i < lists.size(); i++) {
            for (int j = 0; j < lists.get(i).size(); j++) {
                charArray[i][j] = stringToCharMap.get(lists.get(i).get(j));
            }
        }

        return charArray;
    }
}