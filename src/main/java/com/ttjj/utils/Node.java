package com.ttjj.utils;

public class Node implements Comparable<Node> {
    int x, y;
    double gCost, hCost;
    Node parent;

    Node(int x, int y, double gCost, double hCost, Node parent) {
        this.x = x;
        this.y = y;
        this.gCost = gCost;
        this.hCost = hCost;
        this.parent = parent;
    }

    double getFCost() {
        return gCost + hCost;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.getFCost(), other.getFCost());
    }

}
