package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:28
 * @Description: ..
 */
public class Bomb {
    public Position position;
    public int owner_id;
    public String team; // 放置炸弹的糖豆人所属的队伍

    public int range; // 炸弹爆炸的范围
    public int explode_at; // 炸弹爆炸的游戏刻（Tick）

}