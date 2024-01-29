package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.bean.MapBitsPermissionsBo;
import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.SpriteWithTypeBo;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.*;

@Slf4j
public class PathFinder {

    /** 定义八个方向的移动，包括斜向 */
    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}, // 上下左右
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // 斜向
    };

    /** 起始点的物理x坐标 */
    private final double physicalX0;

    /** 起始点的物理y坐标 */
    private final double physicalY0;

    /** 终点的物理x坐标 */
    private final double physicalX1;

    /** 终点的物理y坐标 */
    private final double physicalY1;

    /** 起始点的逻辑x坐标 */
    private final int logicalX0;

    /** 起始点的逻辑y坐标 */
    private final int logicalY0;

    /** 终点的逻辑x坐标 */
    private int logicalX1;

    /** 终点的逻辑y坐标 */
    private int logicalY1;

    /** 发起者的ID */
    private final String initiatorId;

    /** 发起者的宽度的一半 */
    private final int initiatorHalfLogicalWidth;

    /** 发起者的高度的一半 */
    private final int initiatorHalfLogicalHeight;

    /** 目标的hashCode（如果目标是建筑） */
    @Nullable
    private final Integer destBuildingHashCode;

    /** 目标精灵的宽度的一半（如果目标是精灵） */
    @Nullable
    private final Integer destSpriteHalfLogicalWidth;

    /** 目标精灵的高度的一半（如果目标是精灵） */
    @Nullable
    private final Integer destSpriteHalfLogicalHeight;

    /** 是否是直线移动 */
    private final boolean straightMove;

    /** 是否保持一段距离 */
    private final boolean keepDistance;

    /** 地图点权限 */
    private final MapBitsPermissionsBo permissions;

    public PathFinder(SpriteWithTypeBo initiator, MoveBo moveBo, MapBitsPermissionsBo permissions) {
        physicalX0 = initiator.getX();
        physicalY0 = initiator.getY();
        physicalX1 = moveBo.getX();
        physicalY1 = moveBo.getY();
        // 将物理坐标转换为地图坐标
        logicalX0 = physicalAxisToLogicalAxis(physicalX0);
        logicalY0 = physicalAxisToLogicalAxis(physicalY0);
        logicalX1 = physicalAxisToLogicalAxis(physicalX1);
        logicalY1 = physicalAxisToLogicalAxis(physicalY1);
        initiatorId = initiator.getId();
        double initiatorPhysicalWidth = initiator.getWidth() * initiator.getWidthRatio();
        double initiatorPhysicalHeight = initiator.getHeight() * initiator.getHeightRatio();
        // 将物品宽高的像素转换为地图坐标
        initiatorHalfLogicalWidth = physicalSizeToLogicalSize(initiatorPhysicalWidth) / 2;
        initiatorHalfLogicalHeight = physicalSizeToLogicalSize(initiatorPhysicalHeight) / 2;
        // 如果目标是建筑物
        destBuildingHashCode = Optional.ofNullable(moveBo.getDestBuildingId()).map(String::hashCode).orElse(null);
        // 如果目标是精灵
        if (moveBo.getDestSprite() != null) {
            // 此时重点被修正为精灵中心点
            logicalX1 = (int) (moveBo.getDestSprite().getX() / Constants.PIXELS_PER_GRID);
            logicalY1 = (int) (moveBo.getDestSprite().getY() / Constants.PIXELS_PER_GRID);
            // 获取精灵的宽高
            double destSpritePhysicalWidth = moveBo.getDestSprite().getWidth() * moveBo.getDestSprite().getWidthRatio();
            double destSpritePhysicalHeight = moveBo.getDestSprite().getHeight() * moveBo.getDestSprite().getHeightRatio();
            // 将物品宽高的像素转换为地图坐标
            destSpriteHalfLogicalWidth = physicalSizeToLogicalSize(destSpritePhysicalWidth) / 2;
            destSpriteHalfLogicalHeight = physicalSizeToLogicalSize(destSpritePhysicalHeight) / 2;
        } else {
            destSpriteHalfLogicalWidth = null;
            destSpriteHalfLogicalHeight = null;
        }
        straightMove = moveBo.isStraightMove();
        keepDistance = moveBo.isKeepDistance();
        this.permissions = permissions;
    }

    /** 寻找路径 */
    public List<Point> find() {
        List<Point> path;
        if (straightMove) {
            path = findStraightPath();
        } else {
            path = findAStarPath();
        }
        if (path.isEmpty()) {
            log.info("找不到路径，发起者：{}, 起点：x={}, y={}，终点：x={}, y={}", initiatorId, logicalX0, logicalY0, logicalX1, logicalY1);
        }
        return path;
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
    private int heuristic(int x1, int y1, int x2, int y2) {
        return (int) (10 * Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)));
    }

    /** 判断给定的坐标是否在地图范围外 */
    private boolean isOutOfBound(int x, int y) {
        return x < 0 || x >= GameCache.map.length || y < 0 || y >= GameCache.map[0].length;
    }

    /** 判断给定点是否是障碍物 */
    private boolean isObstacle(int mapBits) {
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
    private boolean isObstacle(int x, int y) {
        // 如果坐标在目标点附近（距离小于物体大小），并且该点本身并非障碍物，那么直接认为可以容下物体
        if (Math.abs(x - logicalX1) <= initiatorHalfLogicalWidth && Math.abs(y - logicalY1) <= initiatorHalfLogicalHeight
                && !isObstacle(GameCache.map[x][y])) {
            return false;
        }

        // 如果坐标是起点附近
        if (Math.abs(x - logicalX0) <= 1 && Math.abs(y - logicalY0) <= 1) {
            return false;
        }

        // 由于物体本身占据一定长宽，因此在这里需要判断物体所占据的空间内是否有障碍物
        // 为方便起见，这里只判断了物体中央的十字架和左上角、左下角、右上角、右下角四个点
        List<Point> points = new ArrayList<>();
        points.add(new Point(x, y));
        points.add(new Point(x - initiatorHalfLogicalWidth, y - initiatorHalfLogicalHeight));
        points.add(new Point(x - initiatorHalfLogicalWidth, y + initiatorHalfLogicalHeight));
        points.add(new Point(x + initiatorHalfLogicalWidth, y - initiatorHalfLogicalHeight));
        points.add(new Point(x + initiatorHalfLogicalWidth, y + initiatorHalfLogicalHeight));
        // 添加物体中央的十字架上的点
        for (int i = x - initiatorHalfLogicalWidth; i <= x + initiatorHalfLogicalWidth; i++) {
            points.add(new Point(i, y));
        }
        for (int i = y - initiatorHalfLogicalHeight; i <= y + initiatorHalfLogicalHeight; i++) {
            points.add(new Point(x, i));
        }

        // 判断这些点是否有障碍物
        for (Point point : points) {
            // 如果该点不在地图范围内，则不能容下物体
            if (isOutOfBound(point.getX(), point.getY())) {
                return true;
            }
            // 或者该点是障碍物，并且不是目标建筑，那么不能容下物体
            boolean isObstacle = isObstacle(GameCache.map[point.getX()][point.getY()]);
            if (isObstacle && (destBuildingHashCode == null || GameCache.buildingsHashCodeMap[point.getX()][point.getY()] != destBuildingHashCode)) {
                return true;
            }
        }
        return false;

    }

    /** 判断是否是终点 */
    private boolean isDestination(int x, int y) {
        // 如果精确地到达了终点，那么就是终点
        if (x == logicalX1 && y == logicalY1) {
            return true;
        }

        // 如果终点是建筑
        if (destBuildingHashCode != null) {
            // 如果当前坐标是建筑内部，那么就是终点
            if (GameCache.buildingsHashCodeMap[x][y] == destBuildingHashCode) {
                return true;
            }
        }

        // 如果终点是精灵
        if (destSpriteHalfLogicalWidth != null && destSpriteHalfLogicalHeight != null) {
            // 如果发起者精灵和目标精灵稍稍碰撞，则视作到达终点
            if (Math.abs(x - logicalX1) <= (initiatorHalfLogicalWidth + destSpriteHalfLogicalWidth) - 1
                    && Math.abs(y - logicalY1) <= (initiatorHalfLogicalHeight + destSpriteHalfLogicalHeight) - 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将路径中的冗余点去掉
     */
    private List<Point> removeRedundancyPoints(List<Point> path) {
        // 如果终点是建筑物，那么提前几步终止，防止到达终点后因为卡进建筑而抖动
        int removeLen = Math.max(initiatorHalfLogicalWidth, initiatorHalfLogicalHeight);
        if (destBuildingHashCode != null) {
            path = path.subList(0, Math.max(0, path.size() - removeLen));
        }
        // 如果保持距离
        if (keepDistance) {
            // 去除后面一段
            int minLen = initiatorHalfLogicalWidth * 5;
            if (path.size() < minLen) {
                return Collections.emptyList();
            }
            // 去掉后面一段
            return path.subList(0, path.size() - minLen);
        }
        return path;
    }

    private List<Point> findAStarPath() {
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<Node> closedList = new HashSet<>();

        Node startNode = new Node(logicalX0, logicalY0, null, 0, heuristic(logicalX0, logicalY0, logicalX1, logicalY1));
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            // 如果已经访问过了，就跳过
            if (closedList.contains(currentNode)) {
                continue;
            }

            // 如果到达了目标点，或者当前点的hashcode与终点的hashcode相同，就返回路径
            if (isDestination(currentNode.x, currentNode.y)) {
                List<Point> path = new ArrayList<>();
                while (currentNode != null) {
                    path.add(new Point(currentNode.x, currentNode.y));
                    currentNode = currentNode.parent;
                }
                Collections.reverse(path);

                return removeRedundancyPoints(logicalPointsToPhysicalPoints(path));
            }

            closedList.add(currentNode);

            for (int[] direction : DIRECTIONS) {
                int newX = currentNode.x + direction[0];
                int newY = currentNode.y + direction[1];

                if (isOutOfBound(newX, newY) || isObstacle(newX, newY)) {
                    continue;
                }

                // 计算新的gCost
                int gConst = currentNode.gCost + ((direction[0] == 0 || direction[1] == 0) ? 10 : 14);

                Node neighbor = new Node(newX, newY, currentNode, gConst, heuristic(newX, newY, logicalX1, logicalY1));

                if (closedList.contains(neighbor)) {
                    continue;
                }

                openList.add(neighbor);
            }
        }
        return Collections.emptyList();
    }

    /** 寻找直线路径 */
    public List<Point> findStraightPath() {
        // 计算射线角度
        double angle = Math.atan2(physicalY1 - physicalY0, physicalX1 - physicalX0);
        // x1是否在x0的右边
        boolean x1OnTheRight = physicalX1 > physicalX0;
        // 计算从起点到终点的每个点（每个点之间的x坐标间隔PIXELS_PER_GRID / 2）
        List<Point> points = new ArrayList<>();
        for (double x = physicalX0, y = physicalY0;
             x1OnTheRight ? x <= physicalX1 : x >= physicalX1;
             x += Math.cos(angle) * Constants.PIXELS_PER_GRID / 2, y += Math.sin(angle) * Constants.PIXELS_PER_GRID / 2) {
            int logicalX = physicalAxisToLogicalAxis(x);
            int logicalY = physicalAxisToLogicalAxis(y);
            // 如何不合法或者是障碍物，那么就不再继续
            if (isOutOfBound(logicalX, logicalY) || isObstacle(logicalX, logicalY)) {
                break;
            }
            points.add(new Point((int) x, (int) y));
            // 判断是否是终点
            if (isDestination(logicalX, logicalY)) {
                break;
            }
        }
        return removeRedundancyPoints(points);
    }

    /** 将物理坐标转换为逻辑坐标 */
    public int physicalAxisToLogicalAxis(double physicalAxis) {
        return (int) Math.round(physicalAxis) / Constants.PIXELS_PER_GRID;
    }

    /** 将物理高度或宽度转换为逻辑高度或宽度 */
    public int physicalSizeToLogicalSize(double physicalSize) {
        return (int) Math.ceil(physicalSize / Constants.PIXELS_PER_GRID);
    }

    /** 将逻辑点序列转换为物理点序列 */
    public List<Point> logicalPointsToPhysicalPoints(List<Point> path) {
        for (Point point : path) {
            point.setX(point.getX() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
            point.setY(point.getY() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
        }
        return path;
    }
}