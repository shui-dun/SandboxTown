package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.bean.Point;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
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

        @Override
        public int hashCode() {
            // 只使用坐标来计算哈希值
            return Objects.hash(x, y);
        }

        @Override
        public boolean equals(Object obj) {
            // 只比较坐标
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node other = (Node) obj;
            return x == other.x && y == other.y;
        }
    }

    // 计算启发式距离（二范数）
    private static int heuristic(int x1, int y1, int x2, int y2) {
        return (int) (10 * Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    // 判断给定的坐标是否在地图范围内
    private static boolean isValid(int[][] map, int x, int y) {
        return x >= 0 && x < map.length && y >= 0 && y < map[0].length;
    }

    // 判断给定的坐标是否不能容下物体
    private static boolean cannotHold(int[][] map, int x, int y, int itemHalfWidth, int itemHalfHeight, int startX, int startY, int endX, int endY, Integer destinationHashCode) {

        // 如果坐标在出发点或者目标点附近（距离小于物体大小），那么直接认为可以容下物体
        if (Math.abs(x - startX) <= itemHalfWidth && Math.abs(y - startY) <= itemHalfHeight) {
            return false;
        }
        if (Math.abs(x - endX) <= itemHalfWidth && Math.abs(y - endY) <= itemHalfHeight) {
            return false;
        }

        // 由于物体本身占据一定长宽，因此在这里需要判断物体所占据的空间内是否有障碍物，为方便起见，这里只判断了物体的边界的8个点
        // 这8个点分别是左上角、左下角、右上角、右下角、上边中点、下边中点、左边中点、右边中点
        int[][] points = {
                {x, y},
                {x - itemHalfWidth, y - itemHalfHeight},
                {x - itemHalfWidth, y + itemHalfHeight},
                {x + itemHalfWidth, y - itemHalfHeight},
                {x + itemHalfWidth, y + itemHalfHeight},
                {x, y - itemHalfHeight},
                {x, y + itemHalfHeight},
                {x - itemHalfWidth, y},
                {x + itemHalfWidth, y}
        };
        for (int[] point : points) {
            if (!isValid(map, point[0], point[1]) ||
                    (destinationHashCode == null ? map[point[0]][point[1]] != 0 : map[point[0]][point[1]] != 0 && map[point[0]][point[1]] != destinationHashCode)
            ) {
                return true;
            }
        }
        return false;

    }

    /** 寻找路径 */
    public static List<Point> findPath(int[][] map, int startX, int startY, int endX, int endY, int itemHalfWidth, int itemHalfHeight, Integer destinationHashCode) {
        log.info("destinationHashCode: {}", destinationHashCode);
        log.info("startX: {}, startY: {}, endX: {}, endY: {}", startX, startY, endX, endY);
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

            // 如果到达了目标点，或者当前点的hashcode与终点的hashcode相同，就返回路径
            if (currentNode.x == endX && currentNode.y == endY ||
                    (destinationHashCode != null && map[currentNode.x][currentNode.y] == destinationHashCode)) {
                List<Point> path = new ArrayList<>();
                while (currentNode != null) {
                    path.add(new Point(currentNode.x, currentNode.y));
                    currentNode = currentNode.parent;
                }
                Collections.reverse(path);
                log.info("closedList length: {}", closedList.size());
                return path;
            }

            closedList.add(currentNode);

            for (int[] direction : DIRECTIONS) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];

                if (!isValid(map, newX, newY) || cannotHold(map, newX, newY, itemHalfWidth, itemHalfHeight, startX, startY, endX, endY, destinationHashCode)) {
                    continue;
                }

                // 计算新的gCost
                int gConst = currentNode.gCost + ((direction[0] == 0 || direction[1] == 0) ? 10 : 14);

                Node neighbor = new Node(newX, newY, currentNode, gConst, heuristic(newX, newY, endX, endY));

                if (closedList.contains(neighbor)) {
                    continue;
                }

                openList.add(neighbor);
            }
        }
        log.info("can not find path");
        return null; // 如果没有找到路径，返回null
    }


}
