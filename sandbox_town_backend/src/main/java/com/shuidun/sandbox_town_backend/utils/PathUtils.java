package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.service.MapService;

import java.util.*;

public class PathUtils {

    // 定义八个方向的移动，包括斜向
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // 上下左右
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // 斜向
    };

    // 节点类，用于表示地图上的一个位置
    private static class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        int gCost, hCost;

        Node(int x, int y, Node parent, int gCost, int hCost) {
            this.x = x;
            this.y = y;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
        }

        int fCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost(), other.fCost());
        }
    }

    // 地图
    private static int[][] map;

    // 计算启发式距离（二范数）
    private static int heuristic(int x1, int y1, int x2, int y2) {
        return (int) (10 * Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    // 判断给定的坐标是否在地图范围内
    private static boolean isValid(int x, int y) {
        return x >= 0 && x < map.length && y >= 0 && y < map[0].length;
    }

    // A*算法实现
    public static List<Point> findPath(int[][] map, int startX, int startY, int endX, int endY) {
        PathUtils.map = map;
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Node> closedList = new HashSet<>();

        Node startNode = new Node(startX, startY, null, 0, heuristic(startX, startY, endX, endY));
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            // 如果已经访问过了，就跳过
            if (closedList.contains(currentNode)) {
                continue;
            }

            if (currentNode.x == endX && currentNode.y == endY) {
                List<Point> path = new ArrayList<>();
                while (currentNode != null) {
                    path.add(new Point(currentNode.x, currentNode.y));
                    currentNode = currentNode.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedList.add(currentNode);

            for (int[] direction : DIRECTIONS) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];

                if (!isValid(newX, newY) || map[newX][newY] != 0) {
                    continue;
                }

                // 计算新的gCost
                int gConst = currentNode.gCost + ((direction[0] == 0 || direction[1] == 0) ? 10 : 14);

                Node neighbor = new Node(newX, newY, currentNode, gConst, heuristic(newX, newY, endX, endY));

                if (closedList.contains(neighbor)) {
                    continue;
                }

                // boolean shouldAddToOpenList = true;
                // for (Node openNode : openList) {
                //     if (openNode.x == newX && openNode.y == newY && openNode.fCost() <= neighbor.fCost()) {
                //         shouldAddToOpenList = false;
                //         break;
                //     }
                // }
                //
                // if (shouldAddToOpenList) {
                //     openList.add(neighbor);
                // }
                openList.add(neighbor);
            }
        }

        return null; // 如果没有找到路径，返回null
    }


}
