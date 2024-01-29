package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.bean.MapBitsPermissionsBo;
import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.SpriteWithTypeBo;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.*;

@Slf4j
public class PathUtils {

    /** 定义八个方向的移动，包括斜向 */
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // 上下左右
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // 斜向
    };

    /** 寻找路径 */
    public static List<Point> findPath(int[][] map, int[][] buildingsHashCodeMap, SpriteWithTypeBo initiator, MoveBo moveBo, MapBitsPermissionsBo permissions) {
        double x0 = initiator.getX();
        double y0 = initiator.getY();
        // 将物理坐标转换为地图坐标
        int startX = physicalAxisToMapAxis(x0);
        int startY = physicalAxisToMapAxis(y0);
        int endX = physicalAxisToMapAxis(moveBo.getX());
        int endY = physicalAxisToMapAxis(moveBo.getY());
        double spriteWidth = initiator.getWidth() * initiator.getWidthRatio();
        double spriteHeight = initiator.getHeight() * initiator.getHeightRatio();
        // 将物品宽高的像素转换为地图坐标
        int initiatorHalfWidth = physicalSizeToMapSize(spriteWidth) / 2;
        int initiatorHalfHeight = physicalSizeToMapSize(spriteHeight) / 2;
        // 如果目标是建筑物
        Integer destinationHashCode = moveBo.getDestBuildingId() == null ? null : moveBo.getDestBuildingId().hashCode();
        // 如果目标是精灵
        Integer destSpriteHalfWidth = null;
        Integer destSpriteHalfHeight = null;
        if (moveBo.getDestSprite() != null) {
            // 此时重点被修正为精灵中心点
            endX = (int) (moveBo.getDestSprite().getX() / Constants.PIXELS_PER_GRID);
            endY = (int) (moveBo.getDestSprite().getY() / Constants.PIXELS_PER_GRID);
            // 获取精灵的宽高
            double destSpriteWidth = moveBo.getDestSprite().getWidth() * moveBo.getDestSprite().getWidthRatio();
            double destSpriteHeight = moveBo.getDestSprite().getHeight() * moveBo.getDestSprite().getHeightRatio();
            // 将物品宽高的像素转换为地图坐标
            destSpriteHalfWidth = (int) Math.ceil(destSpriteWidth / Constants.PIXELS_PER_GRID) / 2;
            destSpriteHalfHeight = (int) Math.ceil(destSpriteHeight / Constants.PIXELS_PER_GRID) / 2;
        }
        if (moveBo.isStraightMove()) {
            return findStraightPath(map, buildingsHashCodeMap, initiator, x0, y0, moveBo.getX(), moveBo.getY(), startX, startY, endX, endY, initiatorHalfWidth, initiatorHalfHeight, destinationHashCode, destSpriteHalfWidth, destSpriteHalfHeight, permissions, moveBo.isKeepDistance());
        } else {
            return findAStarPath(map, buildingsHashCodeMap, initiator, startX, startY, endX, endY, initiatorHalfWidth, initiatorHalfHeight, destinationHashCode, destSpriteHalfWidth, destSpriteHalfHeight, permissions, moveBo.isKeepDistance());
        }
    }

    /** 节点类，用于表示地图上的一个位置 */
    private static class Node implements Comparable<Node> {
        int x, y;
        @Nullable
        Node parent;
        int gCost, hCost;

        Node(int x, int y, @Nullable Node parent, int gCost, int hCost) {
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

    /** 计算启发式距离（二范数） */
    private static int heuristic(int x1, int y1, int x2, int y2) {
        return (int) (10 * Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    /** 判断给定的坐标是否在地图范围内 */
    private static boolean isValid(int[][] map, int x, int y) {
        return x >= 0 && x < map.length && y >= 0 && y < map[0].length;
    }

    /** 判断给定点是否是障碍物 */
    private static boolean obstacle(int mapBits, MapBitsPermissionsBo permissions) {
        // 如果是障碍物，那么直接返回true
        if ((mapBits & permissions.getObstacles()) != 0) {
            return true;
        }
        // 如果需要进行allow判断
        if (permissions.getAllow() != 0) {
            // 如果是允许的，那么返回false
            return (mapBits & permissions.getAllow()) == 0;
        }
        // 如果是禁止的，那么返回true
        return (mapBits & permissions.getForbid()) != 0;
    }

    /** 判断给定的坐标是否不能容下物体 */
    private static boolean obstacle(
            int[][] map, int[][] buildingsHashCodeMap,
            int x, int y, int itemHalfWidth, int itemHalfHeight,
            int startX, int startY, int endX, int endY, @Nullable Integer destinationHashCode,
            MapBitsPermissionsBo permissions) {

        // 如果坐标在目标点附近（距离小于物体大小），并且该点本身并非障碍物，那么直接认为可以容下物体
        if (Math.abs(x - endX) <= itemHalfWidth && Math.abs(y - endY) <= itemHalfHeight
                && !obstacle(map[x][y], permissions)) {
            return false;
        }

        // 如果坐标是起点附近
        if (Math.abs(x - startX) <= 1 && Math.abs(y - startY) <= 1) {
            return false;
        }

        // 由于物体本身占据一定长宽，因此在这里需要判断物体所占据的空间内是否有障碍物
        // 为方便起见，这里只判断了物体中央的十字架和左上角、左下角、右上角、右下角四个点
        List<Point> points = new ArrayList<>();
        points.add(new Point(x, y));
        points.add(new Point(x - itemHalfWidth, y - itemHalfHeight));
        points.add(new Point(x - itemHalfWidth, y + itemHalfHeight));
        points.add(new Point(x + itemHalfWidth, y - itemHalfHeight));
        points.add(new Point(x + itemHalfWidth, y + itemHalfHeight));
        // 添加物体中央的十字架上的点
        for (int i = x - itemHalfWidth; i <= x + itemHalfWidth; i++) {
            points.add(new Point(i, y));
        }
        for (int i = y - itemHalfHeight; i <= y + itemHalfHeight; i++) {
            points.add(new Point(x, i));
        }

        // 判断这些点是否有障碍物
        for (Point point : points) {
            // 如果该点不在地图范围内，则不能容下物体
            if (!isValid(map, point.getX(), point.getY())) {
                return true;
            }
            // 或者该点是障碍物，并且不是目标建筑，那么不能容下物体
            boolean isObstacle = obstacle(map[point.getX()][point.getY()], permissions);
            if (isObstacle && (destinationHashCode == null || buildingsHashCodeMap[point.getX()][point.getY()] != destinationHashCode)) {
                return true;
            }
        }
        return false;

    }

    /** 判断是否是终点 */
    private static boolean isDestination(
            int[][] buildingsHashCodeMap,
            int x, int y, int endX, int endY,
            int initiatorHalfWidth, int initiatorHalfHeight,
            @Nullable Integer destinationHashCode,
            @Nullable Integer destSpriteHalfWidth,
            @Nullable Integer destSpriteHalfHeight) {
        // 如果精确地到达了终点，那么就是终点
        if (x == endX && y == endY) {
            return true;
        }

        // 如果终点是建筑
        if (destinationHashCode != null) {
            // 如果当前坐标是建筑内部，那么就是终点
            if (buildingsHashCodeMap[x][y] == destinationHashCode) {
                return true;
            }
        }

        // 如果终点是精灵
        if (destSpriteHalfWidth != null && destSpriteHalfHeight != null) {
            // 如果发起者精灵和目标精灵稍稍碰撞，则视作到达终点
            if (Math.abs(x - endX) <= (initiatorHalfWidth + destSpriteHalfWidth) - 1
                    && Math.abs(y - endY) <= (initiatorHalfHeight + destSpriteHalfHeight) - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 后处理路径，将路径中的冗余点去掉
     *
     * @param physicalAxis 是否已经是物理坐标，如果不是，那么会将地图坐标转换为物理坐标
     */
    private static List<Point> postProcessPath(boolean physicalAxis, SpriteWithTypeBo initiator, List<Point> path, int startX, int startY, int endX, int endY, int initiatorHalfWidth, int initiatorHalfHeight, boolean keepDistance, @Nullable Integer destinationHashCode) {
        // 如果终点是建筑物，那么提前几步终止，防止到达终点后因为卡进建筑而抖动
        int removeLen = Math.max(initiatorHalfWidth, initiatorHalfHeight);
        if (destinationHashCode != null) {
            path = path.subList(0, Math.max(0, path.size() - removeLen));
        }
        if (path.isEmpty()) {
            log.info("找不到路径，发起者：{}, 起点：x={}, y={}，终点：x={}, y={}", initiator.getId(), startX, startY, endX, endY);
        }
        // 将地图坐标转换为物理坐标
        // 一般来说，地图坐标是整数，而物理坐标是浮点数
        // 但是显然在这里计算得到的物理坐标也是整数
        if (!physicalAxis) {
            for (Point point : path) {
                point.setX(point.getX() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
                point.setY(point.getY() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
            }
        }
        // 如果保持距离
        if (keepDistance) {
            // 去除后面一段
            int minLen = (int) (initiator.getWidth() * initiator.getWidthRatio() * 2.5 / Constants.PIXELS_PER_GRID);
            if (path.size() < minLen) {
                return Collections.emptyList();
            }
            // 去掉后面一段
            return path.subList(0, path.size() - minLen);
        }
        return path;
    }

    /**
     * 寻找路径
     * 以下坐标全都是指逻辑坐标，而非像素坐标
     *
     * @param map                  地图
     * @param buildingsHashCodeMap 建筑物的hashcode地图，如果某个点是建筑物，则该点的值为建筑物的hashcode，否则为0
     * @param initiator            发起者
     * @param permissions          精灵的权限
     * @return 路径，如果没找到，返回空列表
     */
    public static List<Point> findAStarPath(
            int[][] map, int[][] buildingsHashCodeMap,
            SpriteWithTypeBo initiator,
            int startX, int startY,
            int endX, int endY,
            int initiatorHalfWidth, int initiatorHalfHeight,
            @Nullable Integer destinationHashCode,
            @Nullable Integer destSpriteHalfWidth,
            @Nullable Integer destSpriteHalfHeight,
            MapBitsPermissionsBo permissions,
            boolean keepDistance) {
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
            if (isDestination(buildingsHashCodeMap, currentNode.x, currentNode.y, endX, endY,
                    initiatorHalfWidth, initiatorHalfHeight,
                    destinationHashCode, destSpriteHalfWidth, destSpriteHalfHeight)) {
                List<Point> path = new ArrayList<>();
                while (currentNode != null) {
                    path.add(new Point(currentNode.x, currentNode.y));
                    currentNode = currentNode.parent;
                }
                Collections.reverse(path);

                return postProcessPath(false, initiator, path, startX, startY, endX, endY, initiatorHalfWidth, initiatorHalfHeight, keepDistance, destinationHashCode);
            }

            closedList.add(currentNode);

            for (int[] direction : DIRECTIONS) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];

                if (!isValid(map, newX, newY) || obstacle(map, buildingsHashCodeMap, newX, newY, initiatorHalfWidth, initiatorHalfHeight, startX, startY, endX, endY, destinationHashCode, permissions)) {
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
        return Collections.emptyList();
    }

    /** 寻找直线路径 */
    public static List<Point> findStraightPath(
            int[][] map, int[][] buildingsHashCodeMap,
            SpriteWithTypeBo initiator,
            double x0, double y0, double x1, double y1,
            int startX, int startY,
            int endX, int endY,
            int initiatorHalfWidth, int initiatorHalfHeight,
            @Nullable Integer destinationHashCode,
            @Nullable Integer destSpriteHalfWidth,
            @Nullable Integer destSpriteHalfHeight,
            MapBitsPermissionsBo permissions,
            boolean keepDistance) {
        // 计算射线角度
        double angle = Math.atan2(y1 - y0, x1 - x0);
        // x1是否在x0的右边
        boolean x1OnTheRight = x1 > x0;
        // 计算从起点到终点的每个点（每个点之间的x坐标间隔PIXELS_PER_GRID / 2）
        List<Point> points = new ArrayList<>();
        for (double x = x0, y = y0;
             x1OnTheRight ? x <= x1 : x >= x1;
             x += Math.cos(angle) * Constants.PIXELS_PER_GRID / 2, y += Math.sin(angle) * Constants.PIXELS_PER_GRID / 2) {
            int logicalX = physicalAxisToMapAxis(x);
            int logicalY = physicalAxisToMapAxis(y);
            // 如何不合法或者是障碍物，那么就不再继续
            if (!isValid(map, logicalX, logicalY) || obstacle(map, buildingsHashCodeMap, logicalX, logicalY, initiatorHalfWidth, initiatorHalfHeight, startX, startY, endX, endY, destinationHashCode, permissions)) {
                break;
            }
            points.add(new Point((int) x, (int) y));
            // 判断是否是终点
            if (isDestination(buildingsHashCodeMap, logicalX, logicalY, endX, endY,
                    initiatorHalfWidth, initiatorHalfHeight,
                    destinationHashCode, destSpriteHalfWidth, destSpriteHalfHeight)) {
                break;
            }
        }
        return postProcessPath(true, initiator, points, startX, startY, endX, endY, initiatorHalfWidth, initiatorHalfHeight, keepDistance, destinationHashCode);
    }

    /** 将物理坐标转换为地图坐标 */
    public static int physicalAxisToMapAxis(double physicalAxis) {
        return (int) Math.round(physicalAxis) / Constants.PIXELS_PER_GRID;
    }

    /** 将物理高度或宽度转换为地图高度或宽度 */
    public static int physicalSizeToMapSize(double physicalSize) {
        return (int) Math.ceil(physicalSize / Constants.PIXELS_PER_GRID);
    }
}