package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.MapBitEnum;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import com.shuidun.sandbox_town_backend.mapper.GameMapMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.PathFinder;
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
 * 地图相关的服务
 */
@Slf4j
@Service
public class GameMapService {

    private final GameMapMapper gameMapMapper;

    private final BuildingMapper buildingMapper;

    private final BuildingTypeMapper buildingTypeMapper;

    @Value("${mapId}")
    private String mapId;

    /** 建筑类型图片 */
    private final Map<BuildingTypeEnum, BufferedImage> buildingTypesImages = new ConcurrentHashMap<>();

    public GameMapService(GameMapMapper gameMapMapper, BuildingMapper buildingMapper, BuildingTypeMapper buildingTypeMapper) {
        this.gameMapMapper = gameMapMapper;
        this.buildingMapper = buildingMapper;
        this.buildingTypeMapper = buildingTypeMapper;
    }

    /** 向地图指定位置添加一个bit */
    private void addBitToMap(int[][] map, int x, int y, MapBitEnum bit) {
        int bitValue = 1 << bit.ordinal();
        map[x][y] |= bitValue;
    }

    /** 从地图指定位置删除一个bit */
    private void removeBitFromMap(int[][] map, int x, int y, MapBitEnum bit) {
        int bitValue = 1 << bit.ordinal();
        map[x][y] &= ~bitValue;
    }

    /** 判断地图某一点是否为某个bit */
    private boolean isBitInMap(int[][] map, int x, int y, MapBitEnum bit) {
        int bitValue = 1 << bit.ordinal();
        return (map[x][y] & bitValue) != 0;
    }

    /** 判断地图某一点是否为某些bit中的至少一个 */
    private boolean isAnyBitInMap(int[][] map, int x, int y, MapBitEnum... bits) {
        int bitValue = 0;
        for (MapBitEnum bit : bits) {
            bitValue |= 1 << bit.ordinal();
        }
        return (map[x][y] & bitValue) != 0;
    }

    /** 画一个2x2的墙 */
    private void drawWall(int[][] map, int x, int y) {
        addBitToMap(map, 2 * x, 2 * y, MapBitEnum.WALL);
        addBitToMap(map, 2 * x + 1, 2 * y, MapBitEnum.WALL);
        addBitToMap(map, 2 * x, 2 * y + 1, MapBitEnum.WALL);
        addBitToMap(map, 2 * x + 1, 2 * y + 1, MapBitEnum.WALL);
    }

    private void unDrawWall(int[][] map, int x, int y) {
        removeBitFromMap(map, 2 * x, 2 * y, MapBitEnum.WALL);
        removeBitFromMap(map, 2 * x + 1, 2 * y, MapBitEnum.WALL);
        removeBitFromMap(map, 2 * x, 2 * y + 1, MapBitEnum.WALL);
        removeBitFromMap(map, 2 * x + 1, 2 * y + 1, MapBitEnum.WALL);
    }

    /** 生成迷宫 */
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


    /**
     * 寻路算法
     *
     * @param initiator 发起者
     * @param moveBo    移动信息
     * @return 路径节点列表，如果找不到路径，则返回空列表
     */
    public List<Point> findPath(SpriteWithTypeBo initiator, MoveBo moveBo,
                                MapBitsPermissionsBo permissions) {

        // 调用寻路算法
        return new PathFinder(initiator, moveBo, permissions).find();
    }

    /** 查看某建筑是否与其他建筑有重叠，或者超出边界 */
    public boolean isBuildingOverlap(BuildingDo building) {
        // 获取建筑物的左上角的坐标
        double buildingX = building.getOriginX();
        double buildingY = building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        double buildingWidth = building.getWidth();
        double buildingHeight = building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = (int) Math.round(buildingX) / Constants.PIXELS_PER_GRID;
        int buildingLogicalY = (int) Math.round(buildingY) / Constants.PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = (int) Math.round(buildingWidth) / Constants.PIXELS_PER_GRID;
        int buildingLogicalHeight = (int) Math.round(buildingHeight) / Constants.PIXELS_PER_GRID;
        // 判断是否超出边界
        if (buildingLogicalX < 0 || buildingLogicalY < 0 ||
                buildingLogicalX + buildingLogicalWidth > GameCache.map.length || buildingLogicalY + buildingLogicalHeight > GameCache.map[0].length) {
            return true;
        }
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 如果当前格子已有其他建筑（或者围墙）
                if (isAnyBitInMap(GameCache.map, i, j, MapBitEnum.BUILDING, MapBitEnum.WALL)) {
                    // 得到当前格中心的物理坐标
                    int pixelX = i * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                    int pixelY = j * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                    // 获取当前格子中心在整个建筑图中的相比于左上角的比例
                    double ratioX = (pixelX - buildingX) / buildingWidth;
                    double ratioY = (pixelY - buildingY) / buildingHeight;
                    // 如果比例不合法
                    if (ratioX < 0 || ratioX > 1 || ratioY < 0 || ratioY > 1) {
                        continue;
                    }
                    // 获取当前格子中心在建筑物黑白图中的坐标
                    BufferedImage bufferedImage = buildingTypesImages.get(building.getType());
                    assert bufferedImage != null;
                    int buildingPixelX = (int) (ratioX * bufferedImage.getWidth());
                    int buildingPixelY = (int) (ratioY * bufferedImage.getHeight());
                    // 获取当前格子中心的颜色
                    int color = bufferedImage.getRGB(buildingPixelX, buildingPixelY);
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

    /** 严格版本的建筑重叠检测，当前建筑超过边界或者当前建筑所在的矩形区域内有其他建筑，则返回true */
    public boolean isBuildingOverlapStrict(BuildingDo building) {
        // 获取建筑物的左上角的坐标
        double buildingX = building.getOriginX();
        double buildingY = building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        double buildingWidth = building.getWidth();
        double buildingHeight = building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = (int) Math.round(buildingX) / Constants.PIXELS_PER_GRID;
        int buildingLogicalY = (int) Math.round(buildingY) / Constants.PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = (int) Math.round(buildingWidth) / Constants.PIXELS_PER_GRID;
        int buildingLogicalHeight = (int) Math.round(buildingHeight) / Constants.PIXELS_PER_GRID;
        // 判断是否超出边界
        if (buildingLogicalX < 0 || buildingLogicalY < 0 ||
                buildingLogicalX + buildingLogicalWidth > GameCache.map.length || buildingLogicalY + buildingLogicalHeight > GameCache.map[0].length) {
            return true;
        }
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 如果当前格子已有其他建筑（或者围墙）
                if (isAnyBitInMap(GameCache.map, i, j, MapBitEnum.BUILDING, MapBitEnum.WALL)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 建筑类型到“建筑周围”地图点的映射 */
    private static final Map<BuildingTypeEnum, MapBitEnum> BUILDING_TYPE_TO_SURROUNDING_MAP_BIT = Map.of(
            BuildingTypeEnum.TOMBSTONE, MapBitEnum.SURROUNDING_TOMBSTONE,
            BuildingTypeEnum.GREEK_TEMPLE, MapBitEnum.SURROUNDING_GREEK_TEMPLE
    );

    /** 放置建筑 */
    public void placeBuildingOnMap(BuildingDo building) {
        // 获取建筑物的左上角的坐标
        double buildingX = building.getOriginX();
        double buildingY = building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        double buildingWidth = building.getWidth();
        double buildingHeight = building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = (int) Math.round(buildingX) / Constants.PIXELS_PER_GRID;
        int buildingLogicalY = (int) Math.round(buildingY) / Constants.PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = (int) Math.round(buildingWidth) / Constants.PIXELS_PER_GRID;
        int buildingLogicalHeight = (int) Math.round(buildingHeight) / Constants.PIXELS_PER_GRID;
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 得到当前格中心的物理坐标
                int pixelX = i * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                int pixelY = j * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2;
                // 获取当前格子中心在整个建筑图中的相比于左上角的比例
                double ratioX = (pixelX - buildingX) / buildingWidth;
                double ratioY = (pixelY - buildingY) / buildingHeight;
                // 如果比例不合法
                if (ratioX < 0 || ratioX > 1 || ratioY < 0 || ratioY > 1) {
                    continue;
                }
                BufferedImage bufferedImage = buildingTypesImages.get(building.getType());
                assert bufferedImage != null;
                // 获取当前格子中心在建筑物黑白图中的坐标
                int buildingPixelX = (int) (ratioX * bufferedImage.getWidth());
                int buildingPixelY = (int) (ratioY * bufferedImage.getHeight());
                // 获取当前格子中心的颜色
                int color = bufferedImage.getRGB(buildingPixelX, buildingPixelY);
                // 如果当前格子中心是黑色
                if (color == Color.BLACK.getRGB()) {
                    // 将当前格子设置为建筑物的id的哈希码
                    GameCache.buildingsHashCodeMap[i][j] = building.getId().hashCode();
                    // 设置当前格子为建筑物
                    addBitToMap(GameCache.map, i, j, MapBitEnum.BUILDING);
                }
            }
        }
        // 标记当前建筑的周围
        MapBitEnum mapBit = BUILDING_TYPE_TO_SURROUNDING_MAP_BIT.get(building.getType());
        if (mapBit == null) {
            return;
        }
        int radius = 8; // 圆的半径
        int centerX = buildingLogicalX + buildingLogicalWidth / 2; // 圆心的x坐标
        int centerY = buildingLogicalY + buildingLogicalHeight / 2; // 圆心的y坐标

        for (int i = 0; i < GameCache.map.length; ++i) {
            for (int j = 0; j < GameCache.map[0].length; ++j) {
                // 计算当前点到圆心的距离
                int distance = (int) Math.sqrt(Math.pow(i - centerX, 2) + Math.pow(j - centerY, 2));
                if (distance <= radius) {
                    addBitToMap(GameCache.map, i, j, mapBit);
                }
            }
        }
    }

    /**
     * 将所有建筑物放置在地图上
     *
     * @return 是否至少存在一个建筑物
     */
    public boolean placeAllBuildingsOnMap() {
        // 建筑物的黑白图的字典
        var buildingTypes = buildingTypeMapper.selectList(null);
        for (BuildingTypeDo buildingType : buildingTypes) {
            BuildingTypeEnum buildingTypeId = buildingType.getId();
            String imagePath = buildingType.getImagePath();
            try {
                BufferedImage image = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
                buildingTypesImages.put(buildingTypeId, image);
            } catch (IOException e) {
                log.info("读取建筑物黑白图失败", e);
            }
        }

        // 获取当前地图上的所有建筑物
        var buildings = buildingMapper.selectByMapId(mapId);

        // 将建筑放置在地图上
        for (BuildingDo building : buildings) {
            placeBuildingOnMap(building);
        }
        return !buildings.isEmpty();
    }

    /** 得到地图信息 */
    public GameMapDo getGameMap() {
        GameMapDo gameMap = gameMapMapper.selectById(mapId);
        assert gameMap != null;
        return gameMap;
    }
}
