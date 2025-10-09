package com.entity;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:27
 * @Description: ..
 */
// 额外状态类
public class ExtraStatusVo {
    public String name; // INV：无敌状态，THB：穿越炸弹状态，LIT：闪电状态，SSW：灵魂互换状态，RUN：糖豆快跑状态，FGT：战斗爽状态
    public int expire_at; // 当前状态将消失的游戏刻（Tick）

}