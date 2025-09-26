package com.entity;

import java.util.List;

/**
 * @Author: zhuLi
 * @Date: 2025-09-26 15:28
 * @Description: ..
 */
// 游戏状态类
public class GameState {
    public String current_match_id;
    public int current_tick;
    public List<List<Cell>> map;
    public PlayerStateVo my_player;
    public List<PlayerStateVo> other_players;
    public List<Bomb> bombs;
    public List<MapItem> map_items;
    // 全图事件 N：无事件，SB：糖豆快跑，BR：障碍破碎，BT：战斗、爽，IR：道具雨，IM：无敌时刻，SS：灵魂互换，BS：炸弹风暴，LB：闪电豆
    public String random_event_state;
}
