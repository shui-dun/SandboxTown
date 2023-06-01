package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进行地图相关的操作，例如寻路算法
 */
@Slf4j
@Service
public class MapService {

    // 建筑物的黑白图的字典
    private Map<String, BufferedImage> buildingTypes = new ConcurrentHashMap<>();

    private List<Building> buildings;

    private final BuildingMapper buildingMapper;

    private final int mapPixelWidth = 1900;

    private final int mapPixelHeight = 1000;

    private final int pixelsPerGrid = 25;

    /** 地图，用于寻路算法，0表示可以通过，非0表示障碍物ID的哈希值 */
    private int[][] map = new int[mapPixelWidth / pixelsPerGrid][mapPixelHeight / pixelsPerGrid];

    private String mapName;

    public MapService(BuildingMapper buildingMapper, @Value("${mapName}") String mapName) {
        // 从配置文件中获取地图名称（使用构建造注入而非变量注入，否则为null）
        this.mapName = mapName;
        log.info("init map {}", mapName);
        this.buildingMapper = buildingMapper;
        // 从数据库中获取所有建筑类型
        List<BuildingType> buildingTypes = this.buildingMapper.getAllBuildingTypes();

        // 初始化建筑物的黑白图
        for (BuildingType buildingType : buildingTypes) {
            String buildingTypeId = buildingType.getId();
            String imagePath = buildingType.getImagePath();
            try {
                BufferedImage image = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
                this.buildingTypes.put(buildingTypeId, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 获取当前地图上的所有建筑物
        buildings = buildingMapper.getAllBuildingsByMapName(mapName);

        // 构建地图（用于寻路算法）
        generateMap();
    }

    /** 构建地图 */
    public void generateMap() {
        // 遍历地图上每一格
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                // 遍历每一个建筑物
                for (Building building : buildings) {
                    // 获取当前格的中心的像素坐标
                    int pixelX = x * pixelsPerGrid + pixelsPerGrid / 2;
                    int pixelY = y * pixelsPerGrid + pixelsPerGrid / 2;
                    // 获取建筑物的左上角的坐标
                    int buildingX = (int) building.getOriginX();
                    int buildingY = (int) building.getOriginY();
                    // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
                    int buildingWidth = (int) building.getDisplayWidth();
                    int buildingHeight = (int) building.getDisplayHeight();
                    // 如果当前格子在建筑物的范围内
                    if (pixelX >= buildingX && pixelX < buildingX + buildingWidth &&
                            pixelY >= buildingY && pixelY < buildingY + buildingHeight) {
                        // 获取当前格子中心在建筑物黑白图中的坐标
                        int buildingPixelX = (int) ((double) (pixelX - buildingX) / buildingWidth * buildingTypes.get(building.getType()).getWidth());
                        int buildingPixelY = (int) ((double) (pixelY - buildingY) / buildingHeight * buildingTypes.get(building.getType()).getHeight());
                        // 获取当前格子中心的颜色
                        int color = buildingTypes.get(building.getType()).getRGB(buildingPixelX, buildingPixelY);
                        // 如果当前格子中心是黑色
                        if (color == Color.BLACK.getRGB()) {
                            // 将当前格子标记为不可通行
                            map[y][x] = building.hashCode();
                        } else {
                            // 将当前格子标记为可通行
                            map[y][x] = 0;
                        }
                        break;
                    }
                }
            }
        }
    }

    /** 寻路算法 */
    public Path findPath(int x0, int y0, int x1, int y1) {
        log.info("find path from ({}, {}) to ({}, {})", x0, y0, x1, y1);
        // 将物理坐标转换为地图坐标
        int startX = x0 / pixelsPerGrid;
        int startY = y0 / pixelsPerGrid;
        int endX = x1 / pixelsPerGrid;
        int endY = y1 / pixelsPerGrid;
        // 调用寻路算法
        List<Point> path = PathUtils.findPath(map, startX, startY, endX, endY);
        // 判断是否为空
        if (path == null) {
            return null;
        }
        // 将地图坐标转换为物理坐标
        for (Point point : path) {
            point.setX(point.getX() * pixelsPerGrid + pixelsPerGrid / 2);
            point.setY(point.getY() * pixelsPerGrid + pixelsPerGrid / 2);
        }
        return new Path(path, 1);
    }

    public int[][] getMap() {
        return map;
    }

    public String getMapName() {
        return mapName;
    }


    public List<Building> getBuildings() {
        return buildings;
    }

    public int getMapPixelWidth() {
        return mapPixelWidth;
    }

    public int getMapPixelHeight() {
        return mapPixelHeight;
    }

    public MapInfo getMapInfo() {
        return new MapInfo(mapPixelWidth, mapPixelHeight, getBuildings());
    }

    public boolean isNear(Point p1, Point p2) {
        // 计算2范数
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2)) <= (double) pixelsPerGrid / 2;
    }

    public boolean isFar(Point p1, Point p2) {
        // 计算2范数
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2)) > (double) pixelsPerGrid * 2;
    }
}
