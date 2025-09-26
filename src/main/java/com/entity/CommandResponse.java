package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:29
 * @Description: ..
 */
// 命令响应
public class CommandResponse {
    public String direction; // N, U, D, L, R
    public boolean is_place_bomb;
    public Integer stride; // 游戏允许玩家精细控制单次移动的步长，比如糖豆人最终速度为15像素/Tick时，糖豆人当前Tick将默认移动15个像素，不输入此参数、输入0或输入的步长大于15时，则糖豆人将按照默认值移动15个像素，同时也可以通过输入具体的步长，控制糖豆人移动[1,14]像素


    public CommandResponse(String direction, boolean is_place_bomb) {
        this.direction = direction;
        this.is_place_bomb = is_place_bomb;
    }

    public CommandResponse(String direction, boolean is_place_bomb, Integer stride) {
        this.direction = direction;
        this.is_place_bomb = is_place_bomb;
        this.stride = stride;
    }
}