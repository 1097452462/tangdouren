package com.ttjj.controller;

import com.ttjj.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class PlayerController {
    @Autowired
    PlayerService playerService;

    @PostMapping("/play")
    public Map<String,String> getStep(@RequestBody Map<String,Object> entity){
        Map<String, String> result = playerService.getStep(entity);
        return result;
    }
}
