package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:27
 * @Description: ..
 */
public class PixelPosition {
    public int x;
    public int y;

    public PixelPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // 将像素坐标转换为格子坐标
    public Position toGridPosition() {
        return new Position((x - 25) / 50, (y - 25) / 50);
    }
}
