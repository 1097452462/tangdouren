package com.controller;

import com.entity.CommandResponse;
import com.entity.GameState;
import com.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class PlayerController {
    @Autowired
    PlayerService playerService;

    @PostMapping("/command")
    public CommandResponse getStep(@RequestBody GameState gameState){
        try {
            // 在90ms内做出决策
            return playerService.makeDecision(gameState);
        } catch (Exception e) {
            // 发生异常时返回安全操作
            return new CommandResponse("N", false);
        }
    }

    @RequestMapping(value = "/ping", method = RequestMethod.HEAD)
    public ResponseEntity<Void> handlePing() {
        // 50ms内响应连通性检查
        return ResponseEntity.ok().build();
    }
}
