package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:27
 * @Description: ..
 */
// 额外状态类
public class ExtraStatusVo {
    public String type; // INV, THB, LIT, SSW, FGT
    public int expire_at; // 当前状态将消失的游戏刻（Tick）

}