package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:26
 * @Description: ..
 */
public class Position {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Position other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }
}