package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:27
 * @Description: ..
 */
// 地图格子类
public class Cell {
    public String terrain;   // P：平地，B：加速点，M：减速点，I：不可破坏障碍物，N：不可破坏障碍物，D：可破坏障碍物

    public String ownership; // N：未被占领，R：红队占领，B：蓝队占领

    public int owner_id;
}