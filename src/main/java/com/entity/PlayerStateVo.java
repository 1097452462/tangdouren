package com.entity;

import java.util.List;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:28
 * @Description: ..
 */
// 玩家状态类
public class PlayerStateVo {
    public int id;
    public String name;
    public String team; // R：红队，B：蓝队
    public String status; // A：正常状态，D：炸晕状态
    public List<ExtraStatusVo> extra_status; // INV：无敌状态，THB：穿越炸弹状态，LIT：闪电状态，SSW：灵魂互换状态，RUN：糖豆快跑状态，FGT：战斗爽状态
    public PixelPosition position; // 糖豆人位置，中心点像素坐标
    public String direction; // N：无方向/静止，U：上，D：下，L：左，R：右
    public int bomb_pack_count; // 糖豆人拥有的炸弹背包数量

    public int sweet_potion_count; // 糖豆人拥有的强化药水数量

    public int agility_boots_count; // 糖豆人拥有的灵巧飞靴数量


    // 获取当前格子坐标
    public Position getGridPosition() {
        return position.toGridPosition();
    }

    // 检查是否无敌
    public boolean isInvincible() {
        return extra_status.stream().anyMatch(status -> "INV".equals(status.name));
    }

    // 检查是否可以穿越炸弹
    public boolean canPassBombs() {
        return extra_status.stream().anyMatch(status -> "THB".equals(status.name));
    }

    // 获取移动速度（格/秒）
    public double getMoveSpeed() {
        double baseSpeed = 2.0; // 2格/秒
        double bootBonus = agility_boots_count * 0.4; // 每个飞靴+0.4格/秒
        return Math.min(baseSpeed + bootBonus, 3.2); // 上限3.2格/秒
    }
}
