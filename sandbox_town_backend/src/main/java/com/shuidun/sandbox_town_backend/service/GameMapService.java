package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Building;
import com.shuidun.sandbox_town_backend.bean.BuildingType;
import com.shuidun.sandbox_town_backend.bean.GameMap;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import com.shuidun.sandbox_town_backend.mapper.GameMapMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.NameGenerator;
import com.shuidun.sandbox_town_backend.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地图相关的服务
 */
@Slf4j
@Service
public class GameMapService {

    private final BuildingMapper buildingMapper;

    private final BuildingTypeMapper buildingTypeMapper;

    private final GameMapMapper gameMapMapper;

    private final SpriteService spriteService;

    private final TreeService treeService;

    private final StoreService storeService;

    @Value("${mapId}")
    private String mapId;

    public GameMapService(BuildingMapper buildingMapper, BuildingTypeMapper buildingTypeMapper, GameMapMapper gameMapMapper, SpriteService spriteService, TreeService treeService, StoreService storeService) {
        this.buildingTypeMapper = buildingTypeMapper;
        this.gameMapMapper = gameMapMapper;
        this.buildingMapper = buildingMapper;
        this.spriteService = spriteService;
        this.treeService = treeService;
        this.storeService = storeService;
    }

    // 画一个2x2的墙
    private void drawWall(int[][] map, int x, int y) {
        map[2 * x][2 * y] = 1;
        map[2 * x + 1][2 * y] = 1;
        map[2 * x][2 * y + 1] = 1;
        map[2 * x + 1][2 * y + 1] = 1;
    }

    private void unDrawWall(int[][] map, int x, int y) {
        map[2 * x][2 * y] = 0;
        map[2 * x + 1][2 * y] = 0;
        map[2 * x][2 * y + 1] = 0;
        map[2 * x + 1][2 * y + 1] = 0;
    }

    // 生成迷宫
    public void generateMaze(int[][] map, int x, int y, int w, int h) {
        if (w < 20 || h < 20) {
            return;
        }

        if (w < 40 || h < 40) {
            if (GameCache.random.nextDouble() < 0.5) {
                return;
            }
        }

        int midX = x + w / 2;
        int midY = y + h / 2;

        // 画水平墙
        for (int i = x; i < x + w; i++) {
            drawWall(map, i, midY);
        }

        // 拆除一部分，以保证可以通行
        int holeLen = 6 + GameCache.random.nextInt(5);
        int beginX = x + GameCache.random.nextInt(w / 2 - holeLen - 1);
        int endX = beginX + holeLen;
        for (int i = beginX; i < endX; i++) {
            unDrawWall(map, i, midY);
        }

        holeLen = 6 + GameCache.random.nextInt(5);
        beginX = x + w / 2 + GameCache.random.nextInt(w / 2 - holeLen - 1);
        endX = beginX + holeLen;
        for (int i = beginX; i < endX; i++) {
            unDrawWall(map, i, midY);
        }

        // 画竖直墙
        for (int i = y; i < y + h; i++) {
            drawWall(map, midX, i);
        }

        holeLen = 6 + GameCache.random.nextInt(5);
        int beginY = y + GameCache.random.nextInt(h / 2 - holeLen - 1);
        int endY = beginY + holeLen;
        for (int i = beginY; i < endY; i++) {
            unDrawWall(map, midX, i);
        }

        holeLen = 6 + GameCache.random.nextInt(5);
        beginY = y + h / 2 + GameCache.random.nextInt(h / 2 - holeLen - 1);
        endY = beginY + holeLen;
        for (int i = beginY; i < endY; i++) {
            unDrawWall(map, midX, i);
        }

        // Recursively generate maze in each quadrant
        generateMaze(map, x, y, w / 2, h / 2);
        generateMaze(map, x + w / 2, y, w / 2, h / 2);
        generateMaze(map, x, y + h / 2, w / 2, h / 2);
        generateMaze(map, x + w / 2, y + h / 2, w / 2, h / 2);
    }

    // 将所有建筑物放置在地图上
    public void placeAllBuildingsOnMap() {
        // 建筑物的黑白图的字典
        var buildingTypes = buildingTypeMapper.selectList(null);
        for (BuildingType buildingType : buildingTypes) {
            String buildingTypeId = buildingType.getId();
            String imagePath = buildingType.getImagePath();
            try {
                BufferedImage image = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
                GameCache.buildingTypesImages.put(buildingTypeId, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 获取当前地图上的所有建筑物
        var buildings = buildingMapper.getAllBuildingsByMapId(mapId);

        // 将建筑放置在地图上
        for (Building building : buildings) {
            placeBuildingOnMap(building);
        }
    }


    /**
     * 寻路算法
     * 如果没有终点物体，那么destinationHashCode为null
     * */
    public List<Point> findPath(int x0, int y0, int x1, int y1, int itemWidth, int itemHeight, Integer destinationHashCode) {
        // 将物理坐标转换为地图坐标
        int startX = x0 / Constants.PIXELS_PER_GRID;
        int startY = y0 / Constants.PIXELS_PER_GRID;
        int endX = x1 / Constants.PIXELS_PER_GRID;
        int endY = y1 / Constants.PIXELS_PER_GRID;
        // 将物品宽高的像素转换为地图坐标
        int itemHalfWidth = (int) Math.ceil((double) itemWidth / Constants.PIXELS_PER_GRID) / 2;
        int itemHalfHeight = (int) Math.ceil((double) itemHeight / Constants.PIXELS_PER_GRID) / 2;
        // 调用寻路算法
        List<Point> path = PathUtils.findPath(GameCache.map, startX, startY, endX, endY, itemHalfWidth, itemHalfHeight, destinationHashCode);
        // 判断是否为空
        if (path == null) {
            return null;
        }
        // 将地图坐标转换为物理坐标
        for (Point point : path) {
            point.setX(point.getX() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
            point.setY(point.getY() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
        }
        return path;
    }

    // 查看某建筑是否与其他建筑有重叠，或者超出边界
    private boolean isBuildingOverlap(Building building) {
        // 获取建筑物的左上角的坐标
        int buildingX = (int) building.getOriginX();
        int buildingY = (int) building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        int buildingWidth = (int) building.getWidth();
        int buildingHeight = (int) building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = buildingX / Constants.PIXELS_PER_GRID;
        int buildingLogicalY = buildingY / Constants.PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = buildingWidth / Constants.PIXELS_PER_GRID;
        int buildingLogicalHeight = buildingHeight / Constants.PIXELS_PER_GRID;
        // 判断是否超出边界
        if (buildingLogicalX < 0 || buildingLogicalY < 0 ||
                buildingLogicalX + buildingLogicalWidth > GameCache.map.length || buildingLogicalY + buildingLogicalHeight > GameCache.map[0].length) {
            return true;
        }
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 如果当前格子已有其他建筑
                if (!(GameCache.map[i][j] == 0 || GameCache.map[i][j] == 1)) {
                    // 得到当前格中心的物理坐标
                    int pixelX = i * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                    int pixelY = j * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                    // 获取当前格子中心在整个建筑图中的相比于左上角的比例
                    double ratioX = (double) (pixelX - buildingX) / buildingWidth;
                    double ratioY = (double) (pixelY - buildingY) / buildingHeight;
                    // 如果比例不合法
                    if (ratioX < 0 || ratioX > 1 || ratioY < 0 || ratioY > 1) {
                        continue;
                    }
                    // 获取当前格子中心在建筑物黑白图中的坐标
                    int buildingPixelX = (int) (ratioX * GameCache.buildingTypesImages.get(building.getType()).getWidth());
                    int buildingPixelY = (int) (ratioY * GameCache.buildingTypesImages.get(building.getType()).getHeight());
                    // 获取当前格子中心的颜色
                    int color = GameCache.buildingTypesImages.get(building.getType()).getRGB(buildingPixelX, buildingPixelY);
                    // 如果当前格子中心是黑色
                    if (color == Color.BLACK.getRGB()) {
                        // 说明当前格子有重叠建筑
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 放置建筑
    private void placeBuildingOnMap(Building building) {
        // 获取建筑物的左上角的坐标
        int buildingX = (int) building.getOriginX();
        int buildingY = (int) building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        int buildingWidth = (int) building.getWidth();
        int buildingHeight = (int) building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = buildingX / Constants.PIXELS_PER_GRID;
        int buildingLogicalY = buildingY / Constants.PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = buildingWidth / Constants.PIXELS_PER_GRID;
        int buildingLogicalHeight = buildingHeight / Constants.PIXELS_PER_GRID;
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 得到当前格中心的物理坐标
                int pixelX = i * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                int pixelY = j * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                // 获取当前格子中心在整个建筑图中的相比于左上角的比例
                double ratioX = (double) (pixelX - buildingX) / buildingWidth;
                double ratioY = (double) (pixelY - buildingY) / buildingHeight;
                // 如果比例不合法
                if (ratioX < 0 || ratioX > 1 || ratioY < 0 || ratioY > 1) {
                    continue;
                }
                // 获取当前格子中心在建筑物黑白图中的坐标
                int buildingPixelX = (int) (ratioX * GameCache.buildingTypesImages.get(building.getType()).getWidth());
                int buildingPixelY = (int) (ratioY * GameCache.buildingTypesImages.get(building.getType()).getHeight());
                // 获取当前格子中心的颜色
                int color = GameCache.buildingTypesImages.get(building.getType()).getRGB(buildingPixelX, buildingPixelY);
                // 如果当前格子中心是黑色
                if (color == Color.BLACK.getRGB()) {
                    // 将当前格子设置为建筑物的id的哈希码
                    GameCache.map[i][j] = building.getId().hashCode();
                }
            }
        }
    }

    // 得到地图信息
    public GameMap getGameMap() {
        GameMap gameMap = gameMapMapper.selectById(mapId);
        gameMap.setData(GameCache.map);
        return gameMap;
    }

    // 初始化地图（建造建筑等）
    public void initGameMap(int nBuildings) {
        // 得到所有建筑类型
        var buildingTypes = buildingTypeMapper.selectList(null);
        // 首先，所有类型的建筑都有一个
        List<BuildingType> buildingTypesToBePlaced = new ArrayList<>(buildingTypes);
        // 计算总稀有度
        double totalRarity = 0;
        for (BuildingType buildingType : buildingTypes) {
            totalRarity += buildingType.getRarity();
        }
        // 随后，根据建筑类型的稀有度，根据轮盘赌算法，随机生成nBuildings-buildingTypes.size()个建筑
        for (int i = 0; i < nBuildings - buildingTypes.size(); ++i) {
            // 计算轮盘赌的随机值
            double randomValue = Math.random() * totalRarity;
            // 计算轮盘赌的结果
            double sum = 0;
            int index = 0;
            for (BuildingType buildingType : buildingTypes) {
                sum += buildingType.getRarity();
                if (sum >= randomValue) {
                    index = buildingTypes.indexOf(buildingType);
                    break;
                }
            }
            // 将轮盘赌的结果加入建筑列表
            buildingTypesToBePlaced.add(buildingTypes.get(index));
        }
        // 生成建筑
        for (int i = 0; i < buildingTypesToBePlaced.size(); ++i) {
            // 建筑类型
            BuildingType buildingType = buildingTypesToBePlaced.get(i);
            // 随机生成建筑的左上角
            int x = (int) (Math.random() * (GameCache.map.length - 8)) * Constants.PIXELS_PER_GRID;
            int y = (int) (Math.random() * (GameCache.map[0].length - 8)) * Constants.PIXELS_PER_GRID;
            // 随机生成建筑的宽高，在基础宽高的基础上波动（0.8倍到1.2倍）
            double scale = Math.random() * 0.4 + 0.8;
            // 创建建筑对象
            Building building = new Building();
            building.setId(NameGenerator.generateItemName(buildingType.getId()));
            building.setType(buildingType.getId());
            building.setMap(mapId);
            building.setLevel(1);
            building.setOriginX(x);
            building.setOriginY(y);
            building.setWidth((int) (buildingType.getBasicWidth() * scale));
            building.setHeight((int) (buildingType.getBasicHeight() * scale));
            // 判断是否与其他建筑重叠
            if (!isBuildingOverlap(building)) {
                // 如果不重叠，添加建筑到数据库
                buildingMapper.insert(building);
                // 放置建筑
                placeBuildingOnMap(building);
                // 如果是树
                if (buildingType.getId().equals("tree")) {
                    // 在树下以一定概率生成几只狗
                    if (Math.random() < 0.3) {
                        // 随机生成狗的数量
                        int nDogs = (int) (Math.random() * 3) + 1;
                        // 随机生成狗的位置
                        for (int j = 0; j < nDogs; ++j) {
                            // 随机生成狗的左上角
                            int dogX = building.getOriginX() + (int) (Math.random() * building.getWidth());
                            int dogY = building.getOriginY() + (int) (Math.random() * building.getHeight());
                            // 创建狗
                            spriteService.generateRandomSprite("dog", null, dogX, dogY);
                        }
                    }
                    treeService.createRandomTree(building.getId());
                } else if (buildingType.getId().equals("store")) { // 如果是商店
                    // 刷新商店商品
                    storeService.refresh(building.getId());
                }
            } else {
                log.info("建筑重叠，重新生成建筑");
                // 如果重叠，重新生成建筑
                --i;
            }

        }
    }

    // 计算两点之间的距离
    public double calcDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
