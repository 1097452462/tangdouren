package com.ttjj.service;

import com.ttjj.utils.AStarAlgorithmUtil;
import com.ttjj.utils.MoveTypeUtil;
import org.springframework.stereotype.Service;
import org.testng.collections.Sets;

import java.util.*;

@Service
public class PlayerService{
    public Map<String,String> getStep(Map<String,Object> entity){
        Map<String,String> result = new HashMap<>();
        String farmerId = entity.get("playerId").toString();
        List<Map<String, Object>> animalStateVos = (List<Map<String, Object>>) entity.get("animalStateVos");
        Integer endX = 0;
        Integer endY = 0;
        Integer startX = 0;
        Integer startY = 0;
        List<Map<String, Object>> farmerStateVos = (List<Map<String, Object>>) entity.get("playerStateVos");
        List<List<String>> map = (List<List<String>>)entity.get("map");
        Set<int[]> users = Sets.newHashSet();
        for(Map<String, Object> farmerStateVo: farmerStateVos){
            if (!Objects.equals(farmerId, farmerStateVo.get("playerId").toString())) {
                Integer stX = (Integer) farmerStateVo.get("y");
                Integer stY = (Integer) farmerStateVo.get("x");
                int[] stGrid = AStarAlgorithmUtil.convertToGridCoordinate(stX, stY);
                users.add(stGrid);
            }
        }
        char[][] grid = AStarAlgorithmUtil.listToCharArray(map);
        for (Map<String, Object> farmerStateVo: farmerStateVos) {
            if (Objects.equals(farmerId, farmerStateVo.get("playerId").toString())) {
                startX = (Integer) farmerStateVo.get("y");
                startY = (Integer) farmerStateVo.get("x");
                int[] startGrid = AStarAlgorithmUtil.convertToGridCoordinate(startX, startY);
                List<Integer> carriedList = (List<Integer>)farmerStateVo.get("carried");
                Integer carrySize = (Integer)farmerStateVo.get("carrySize");
                List<Integer> holdingProps = (List<Integer>)farmerStateVo.get("holdingProps");
                if(holdingProps.contains(3)){
                    AStarAlgorithmUtil.setHasType3Item(true);
                }else {
                    AStarAlgorithmUtil.setHasType3Item(false);
                }
                //背包容量为1或2时，动物装满再交付
                if(carrySize==carriedList.size() || carriedList.size() > 1){
                    //中间位置坐标点的集合
                    List<int[]> fhList = getFH();
                    List<int[]> points = null;
                    for (int[] endGrid:fhList) {
                        List<int[]> tempList = AStarAlgorithmUtil.aStarSearchPoint(grid, startGrid[0], startGrid[1], endGrid[0], endGrid[1],users);
                        if(points==null){
                            points = tempList;
                        }else {
                            if(tempList != null && tempList.size()<points.size()){
                                points = tempList;
                            }
                        }
                    }
                    int[] endPoint;
                    if(points==null || points.size() == 0){
                        endPoint = startGrid;
                    }else {
                        endPoint = points.get(0);
                    }
                    String moveType = MoveTypeUtil.getMoveType(startGrid[0], startGrid[1], endPoint[0], endPoint[1]);
                    moveType = MoveTypeUtil.reviseMoveType(startX,startY,moveType,endPoint);
                    result.put("moveType",moveType);
                    return result;
                }else{
                    List<int[]> points = null;
                    for (Map<String, Object> animal : animalStateVos) {
                        endX = (Integer) animal.get("y");
                        endY = (Integer) animal.get("x");
                        int[] endGrid = AStarAlgorithmUtil.convertToGridCoordinate(endX, endY);
                        List<int[]> tempList = AStarAlgorithmUtil.aStarSearchPoint(grid, startGrid[0], startGrid[1], endGrid[0], endGrid[1],users);
                        if(points==null){
                            points = tempList;
                        }else {
                            if(tempList != null && tempList.size()<points.size()){
                                points = tempList;
                            }
                        }
                    }
                    //道具数据
                    List<Map<String, Object>> propVos = (List<Map<String, Object>>) entity.get("propVos");
                    if (propVos.size() > 0) {
                        for (Map<String, Object> propVo: propVos) {
                            Integer type = Integer.parseInt(propVo.get("type").toString());
                            // 背包可重复捡
                            if(holdingProps.contains(type) && type != 2 && type!=4){
                                continue;
                            }
                            endX = (Integer) propVo.get("y");
                            endY = (Integer) propVo.get("x");
                            int[] endGrid = AStarAlgorithmUtil.convertToGridCoordinate(endX,endY);
                            List<int[]> tempList = AStarAlgorithmUtil.aStarSearchPoint(grid, startGrid[0], startGrid[1], endGrid[0], endGrid[1], users);
                            if(points==null){
                                points = tempList;
                            }else {
                                if(tempList != null && tempList.size()+5<points.size()){
                                    points = tempList;
                                }
                            }
                        }
                    }
                    int[] endPoint;
                    if(points==null || points.size() == 0){
                        endPoint = startGrid;
                    }else {
                        endPoint = points.get(0);
                    }
                    String moveType = MoveTypeUtil.getMoveType(startGrid[0], startGrid[1], endPoint[0], endPoint[1]);
                    moveType = MoveTypeUtil.reviseMoveType(startX,startY,moveType,endPoint);
                    result.put("moveType",moveType);
                    return result;
                }
            }
        }
        result.put("moveType","stop");
        return result;
    }

    public List<int[]> getFH(){
        List<int[]> target = new ArrayList<>();
        target.add(new int[]{7,12});
        target.add(new int[]{8,12});
        target.add(new int[]{7,15});
        target.add(new int[]{8,15});
        target.add(new int[]{6,13});
        target.add(new int[]{6,14});
        target.add(new int[]{9,13});
        target.add(new int[]{9,14});
        return target;
    }

}
