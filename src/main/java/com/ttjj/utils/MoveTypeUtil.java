package com.ttjj.utils;

public class MoveTypeUtil {
    public static String getMoveType(Integer startX,Integer startY,Integer endX,Integer endY){
        String moveType = "";
        int dx = endX - startX;
        int dy = endY - startY;
        if (dx == -1 && dy == 0) {
            moveType = "TOP";
        } else if (dx == 1 && dy == 0) {
            moveType = "DOWN";
        } else if (dx == 0 && dy == -1) {
            moveType = "LEFT";
        } else if (dx == 0 && dy == 1) {
            moveType = "RIGHT";
        }

        return moveType;
    }

    public static String reviseMoveType(Integer x, Integer y, String moveType, int[] endPoint){
        if("LEFT".equals(moveType) || "RIGHT".equals(moveType) ){
            Integer middleNextY = (endPoint[0]+1) * 50 - 25;
            if (x > middleNextY) {
                moveType = "TOP";
            }
            else if (x < middleNextY) {
                moveType = "DOWN";
            }
        }else if("DOWN".equals(moveType) || "TOP".equals(moveType) ){
            Integer middleNextX = (endPoint[1]+1) * 50 - 25;
            if (y < middleNextX) {
                moveType = "RIGHT";
            } else if (y > middleNextX) {
                moveType = "LEFT";
            }
        }
        if("".equals(moveType)){
            Integer middleNextY = (endPoint[0]+1) * 50 - 25;
            Integer middleNextX = (endPoint[1]+1) * 50 - 25;
            if (x > middleNextY && y.equals(middleNextX)) {
                moveType = "TOP";
            }else if (x < middleNextY && y.equals(middleNextX)) {
                moveType = "DOWN";
            }else if (y < middleNextX && x.equals(middleNextY)) {
                moveType = "RIGHT";
            } else if (y > middleNextX && x.equals(middleNextY)) {
                moveType = "LEFT";
            }else {
                moveType = "STOP";
            }
        }
        return moveType;
    }
}
