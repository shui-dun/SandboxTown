package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.MapBitEnum;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import com.shuidun.sandbox_town_backend.mapper.GameMapMapper;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 管理地图，以及其上的建筑、精灵
 */
@Slf4j
@Service
public class MapService {

    /**
     * 表示地图上每个点的元素类型
     * 使用位标记来表示每个点可能包含的多种元素
     * 参见{@link com.shuidun.sandbox_town_backend.enumeration.MapBitEnum}
     */
    private int[][] map = new int[0][0];

    /**
     * 存储地图上每个点的建筑物hashcode
     * 如果地图点不包含建筑物，则该位置的值的后32位为0
     */
    private int[][] buildingsHashCodeMap = new int[0][0];

    /** 地图上一格多少像素 */
    private final int PIXELS_PER_GRID = 30;

    private final GameMapMapper gameMapMapper;

    private final BuildingMapper buildingMapper;

    private final BuildingTypeMapper buildingTypeMapper;

    @Value("${mapId}")
    private String mapId;

    /** 建筑类型图片 */
    private final Map<BuildingTypeEnum, BufferedImage> buildingTypesImages = new ConcurrentHashMap<>();

    private SpriteService spriteService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<BuildingTypeEnum, SpecificBuildingService> specificBuildingServices;

    public MapService(GameMapMapper gameMapMapper, BuildingMapper buildingMapper, BuildingTypeMapper buildingTypeMapper, RedisTemplate<String, Object> redisTemplate, List<SpecificBuildingService> specificBuildingServices) {
        this.gameMapMapper = gameMapMapper;
        this.buildingMapper = buildingMapper;
        this.buildingTypeMapper = buildingTypeMapper;
        this.redisTemplate = redisTemplate;
        this.specificBuildingServices = specificBuildingServices.stream().collect(
                Collectors.toMap(SpecificBuildingService::getType, s -> s)
        );
    }

    public void init() {
        // 获得地图信息
        GameMapDo gameMap = getGameMap();

        // 设置随机数种子
        GameCache.random.setSeed(gameMap.getSeed());

        // 初始化地图
        map = new int[gameMap.getWidth() / PIXELS_PER_GRID][gameMap.getHeight() / PIXELS_PER_GRID];
        buildingsHashCodeMap = new int[gameMap.getWidth() / PIXELS_PER_GRID][gameMap.getHeight() / PIXELS_PER_GRID];

        // 在地图上生成围墙
        generateMaze(map, 0, 0, map.length / 2, map[0].length / 2);

        // 在地图上放置建筑
        boolean containsBuilding = placeAllBuildingsOnMap();

        // 放置NPC
        spriteService.onlineNPCs();

        // 如果没有建筑物，则生成一定数量的建筑物
        if (!containsBuilding) {
            createEnvironment(gameMap.getWidth() * gameMap.getHeight() / 300000);
        }
    }

    /**
     * 寻路算法
     *
     * @param initiator 发起者
     * @param moveBo    移动信息
     * @return 路径节点列表，如果找不到路径，则返回空列表
     */
    public List<Point> findPath(SpriteBo initiator, MoveBo moveBo,
                                MapBitsPermissionsBo permissions) {

        // 调用寻路算法
        return new PathFinder(initiator, moveBo, permissions).find();
    }

    /** 刷新所有可刷新的建筑 */
    public void refreshAllBuildings() {
        for (SpecificBuildingService specificBuildingService : specificBuildingServices.values()) {
            specificBuildingService.refreshAll();
            log.info("refreshAllBuildings: {}", specificBuildingService.getClass().getName());
        }
    }

    /** 得到地图信息 */
    public GameMapDo getGameMap() {
        GameMapDo gameMap = gameMapMapper.selectById(mapId);
        assert gameMap != null;
        return gameMap;
    }

    /** 得到地图信息，并带有地图网格数据 */
    public GameMapWithMap getGameMapWithMap() {
        GameMapDo gameMap = getGameMap();
        GameMapWithMap gameMapWithMap = new GameMapWithMap();
        gameMapWithMap.setId(gameMap.getId());
        gameMapWithMap.setName(gameMap.getName());
        gameMapWithMap.setWidth(gameMap.getWidth());
        gameMapWithMap.setHeight(gameMap.getHeight());
        gameMapWithMap.setData(map);
        return gameMapWithMap;
    }

    /** 随机创建指定数目的建筑以及其附属的生态环境 */
    public void createEnvironment(int nBuildings) {
        // 得到所有建筑类型
        var buildingTypes = buildingTypeMapper.selectList(null);
        // 首先，所有类型的建筑都有一个
        List<BuildingTypeDo> buildingTypesToBePlaced = new ArrayList<>(buildingTypes);
        // 计算总稀有度
        double totalRarity = 0;
        for (BuildingTypeDo buildingType : buildingTypes) {
            totalRarity += buildingType.getRarity();
        }
        // 随后，根据建筑类型的稀有度，根据轮盘赌算法，随机生成nBuildings-buildingTypes.size()个建筑
        for (int i = 0; i < nBuildings - buildingTypes.size(); ++i) {
            // 计算轮盘赌的随机值
            double randomValue = Math.random() * totalRarity;
            // 计算轮盘赌的结果
            double sum = 0;
            int index = 0;
            for (BuildingTypeDo buildingType : buildingTypes) {
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
            BuildingTypeDo buildingType = buildingTypesToBePlaced.get(i);
            // 随机生成建筑的左上角
            double x = Math.random() * (map.length - 8) * PIXELS_PER_GRID;
            double y = Math.random() * (map[0].length - 8) * PIXELS_PER_GRID;
            // 随机生成建筑的宽高，在基础宽高的基础上波动（0.8倍到1.2倍）
            double scale = Math.random() * 0.4 + 0.8;
            // 创建建筑对象
            BuildingDo building = new BuildingDo();
            building.setId(UUIDNameGenerator.generateItemName(buildingType.getId().name()));
            building.setType(buildingType.getId());
            building.setMap(mapId);
            building.setLevel(1);
            building.setOriginX(x);
            building.setOriginY(y);
            building.setWidth(buildingType.getBasicWidth() * scale);
            building.setHeight(buildingType.getBasicHeight() * scale);
            // 判断是否与其他建筑重叠
            if (!isBuildingOverlapStrict(building)) {
                // 如果不重叠，添加建筑到数据库
                buildingMapper.insert(building);
                // 放置建筑
                placeBuildingOnMap(building);
                // 初始化对应的建筑
                SpecificBuildingService specificBuildingService = specificBuildingServices.get(buildingType.getId());
                if (specificBuildingService != null) {
                    specificBuildingService.initBuilding(building);
                }
            } else {
                log.info("建筑重叠，重新生成建筑");
                // 如果重叠，重新生成建筑
                --i;
            }
        }
        // 生成精灵
        spriteService.refreshAllSprites();

        // 删除建筑的缓存
        // 之所以不直接使用@CacheEvict(value = "building::buildings", key = "#mapId")
        // 是为了修复GameInitializer的构造方法中调用createEnvironment时，@CacheEvict注解不生效的问题（看起来一个component必须在构造之后才能使用注解）
        redisTemplate.delete("building::buildings::" + mapId);
    }

    /** 查看某建筑是否与其他建筑有重叠，或者超出边界 */
    private boolean isBuildingOverlap(BuildingDo building) {
        // 获取建筑物的左上角的坐标
        double buildingX = building.getOriginX();
        double buildingY = building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        double buildingWidth = building.getWidth();
        double buildingHeight = building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = (int) Math.round(buildingX) / PIXELS_PER_GRID;
        int buildingLogicalY = (int) Math.round(buildingY) / PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = (int) Math.round(buildingWidth) / PIXELS_PER_GRID;
        int buildingLogicalHeight = (int) Math.round(buildingHeight) / PIXELS_PER_GRID;
        // 判断是否超出边界
        if (buildingLogicalX < 0 || buildingLogicalY < 0 ||
                buildingLogicalX + buildingLogicalWidth > map.length || buildingLogicalY + buildingLogicalHeight > map[0].length) {
            return true;
        }
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 如果当前格子已有其他建筑（或者围墙）
                if (isAnyBitInMap(map, i, j, MapBitEnum.BUILDING, MapBitEnum.WALL)) {
                    // 得到当前格中心的物理坐标
                    int pixelX = i * PIXELS_PER_GRID + PIXELS_PER_GRID / 2;
                    int pixelY = j * PIXELS_PER_GRID + PIXELS_PER_GRID / 2;
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
    private boolean isBuildingOverlapStrict(BuildingDo building) {
        // 获取建筑物的左上角的坐标
        double buildingX = building.getOriginX();
        double buildingY = building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        double buildingWidth = building.getWidth();
        double buildingHeight = building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = (int) Math.round(buildingX) / PIXELS_PER_GRID;
        int buildingLogicalY = (int) Math.round(buildingY) / PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = (int) Math.round(buildingWidth) / PIXELS_PER_GRID;
        int buildingLogicalHeight = (int) Math.round(buildingHeight) / PIXELS_PER_GRID;
        // 判断是否超出边界
        if (buildingLogicalX < 0 || buildingLogicalY < 0 ||
                buildingLogicalX + buildingLogicalWidth > map.length || buildingLogicalY + buildingLogicalHeight > map[0].length) {
            return true;
        }
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 如果当前格子已有其他建筑（或者围墙）
                if (isAnyBitInMap(map, i, j, MapBitEnum.BUILDING, MapBitEnum.WALL)) {
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
    private void placeBuildingOnMap(BuildingDo building) {
        // 获取建筑物的左上角的坐标
        double buildingX = building.getOriginX();
        double buildingY = building.getOriginY();
        // 获取建筑物的宽高（暂时不知道宽和高写反了没有，因为现在的图片都是正方形的）
        double buildingWidth = building.getWidth();
        double buildingHeight = building.getHeight();
        // 获取建筑的左上角的逻辑坐标
        int buildingLogicalX = (int) Math.round(buildingX) / PIXELS_PER_GRID;
        int buildingLogicalY = (int) Math.round(buildingY) / PIXELS_PER_GRID;
        // 获取建筑的宽高的逻辑坐标
        int buildingLogicalWidth = (int) Math.round(buildingWidth) / PIXELS_PER_GRID;
        int buildingLogicalHeight = (int) Math.round(buildingHeight) / PIXELS_PER_GRID;
        // 遍历建筑的每一个格子
        for (int i = buildingLogicalX; i < buildingLogicalX + buildingLogicalWidth; ++i) {
            for (int j = buildingLogicalY; j < buildingLogicalY + buildingLogicalHeight; ++j) {
                // 得到当前格中心的物理坐标
                int pixelX = i * PIXELS_PER_GRID + PIXELS_PER_GRID / 2;
                int pixelY = j * PIXELS_PER_GRID + PIXELS_PER_GRID / 2;
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
                    buildingsHashCodeMap[i][j] = building.getId().hashCode();
                    // 设置当前格子为建筑物
                    addBitToMap(map, i, j, MapBitEnum.BUILDING);
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

        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[0].length; ++j) {
                // 计算当前点到圆心的距离
                int distance = (int) Math.sqrt(Math.pow(i - centerX, 2) + Math.pow(j - centerY, 2));
                if (distance <= radius) {
                    addBitToMap(map, i, j, mapBit);
                }
            }
        }
    }

    /**
     * 将所有建筑物放置在地图上
     *
     * @return 是否至少存在一个建筑物
     */
    private boolean placeAllBuildingsOnMap() {
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
    private void generateMaze(int[][] map, int x, int y, int w, int h) {
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

    @Autowired
    @Lazy
    public void setSpriteService(SpriteService spriteService) {
        this.spriteService = spriteService;
    }

    private class PathFinder {

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

        public PathFinder(SpriteBo initiator, MoveBo moveBo, MapBitsPermissionsBo permissions) {
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
            double initiatorPhysicalWidth = initiator.getWidth() * initiator.getSpriteTypeDo().getWidthRatio();
            double initiatorPhysicalHeight = initiator.getHeight() * initiator.getSpriteTypeDo().getHeightRatio();
            // 将物品宽高的像素转换为地图坐标
            initiatorHalfLogicalWidth = physicalSizeToLogicalSize(initiatorPhysicalWidth) / 2;
            initiatorHalfLogicalHeight = physicalSizeToLogicalSize(initiatorPhysicalHeight) / 2;
            // 如果目标是建筑物
            destBuildingHashCode = Optional.ofNullable(moveBo.getDestBuildingId()).map(String::hashCode).orElse(null);
            // 如果目标是精灵
            if (moveBo.getDestSprite() != null) {
                // 此时重点被修正为精灵中心点
                logicalX1 = (int) (moveBo.getDestSprite().getX() / PIXELS_PER_GRID);
                logicalY1 = (int) (moveBo.getDestSprite().getY() / PIXELS_PER_GRID);
                // 获取精灵的宽高
                double destSpritePhysicalWidth = moveBo.getDestSprite().getWidth() * moveBo.getDestSprite().getSpriteTypeDo().getWidthRatio();
                double destSpritePhysicalHeight = moveBo.getDestSprite().getHeight() * moveBo.getDestSprite().getSpriteTypeDo().getHeightRatio();
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
            return x < 0 || x >= map.length || y < 0 || y >= map[0].length;
        }

        /** 判断给定点是否是障碍物 */
        private boolean isMapBitObstacle(int mapBits) {
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

        /** 判断给定的坐标是否不能容下物体（宽松版本，即如果坐标是起点附近，直接不视为障碍物） */
        private boolean isObstacleLoose(int x, int y) {
            // 如果坐标是起点附近
            if (Math.abs(x - logicalX0) <= 1 && Math.abs(y - logicalY0) <= 1) {
                return false;
            }
            return isObstacleStrict(x, y);
        }

        /** 判断给定的坐标是否不能容下物体（严格版本） */
        private boolean isObstacleStrict(int x, int y) {
            // 如果坐标在目标点附近（距离小于物体大小），并且该点本身并非障碍物，那么直接认为可以容下物体
            if (Math.abs(x - logicalX1) <= initiatorHalfLogicalWidth && Math.abs(y - logicalY1) <= initiatorHalfLogicalHeight
                    && !isMapBitObstacle(map[x][y])) {
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
                boolean isObstacle = isMapBitObstacle(map[point.getX()][point.getY()]);
                if (isObstacle && (destBuildingHashCode == null || buildingsHashCodeMap[point.getX()][point.getY()] != destBuildingHashCode)) {
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
                if (buildingsHashCodeMap[x][y] == destBuildingHashCode) {
                    return true;
                }
            }

            // 如果终点是精灵
            if (destSpriteHalfLogicalWidth != null && destSpriteHalfLogicalHeight != null) {
                // 如果发起者精灵和目标精灵稍稍碰撞，则视作到达终点
                return Math.abs(x - logicalX1) <= (initiatorHalfLogicalWidth + destSpriteHalfLogicalWidth) - 1
                        && Math.abs(y - logicalY1) <= (initiatorHalfLogicalHeight + destSpriteHalfLogicalHeight) - 1;
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

                    if (isOutOfBound(newX, newY) || isObstacleLoose(newX, newY)) {
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
                 x += Math.cos(angle) * PIXELS_PER_GRID / 2, y += Math.sin(angle) * PIXELS_PER_GRID / 2) {
                int logicalX = physicalAxisToLogicalAxis(x);
                int logicalY = physicalAxisToLogicalAxis(y);
                // 如何不合法或者是障碍物，那么就不再继续
                if (isOutOfBound(logicalX, logicalY) || isObstacleStrict(logicalX, logicalY)) {
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
            return (int) Math.round(physicalAxis) / PIXELS_PER_GRID;
        }

        /** 将物理高度或宽度转换为逻辑高度或宽度 */
        public int physicalSizeToLogicalSize(double physicalSize) {
            return (int) Math.ceil(physicalSize / PIXELS_PER_GRID);
        }

        /** 将逻辑点序列转换为物理点序列 */
        public List<Point> logicalPointsToPhysicalPoints(List<Point> path) {
            for (Point point : path) {
                point.setX(point.getX() * PIXELS_PER_GRID + PIXELS_PER_GRID / 2);
                point.setY(point.getY() * PIXELS_PER_GRID + PIXELS_PER_GRID / 2);
            }
            return path;
        }
    }


    /** 计算两点之间的距离 */
    private double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /** 目标精灵是否在源精灵的视野内 */
    public boolean isInSight(SpriteDo source, double targetX, double targetY) {
        return calcDistance(source.getX(), source.getY(), targetX, targetY) <= source.getVisionRange() + source.getVisionRange();
    }

    /**
     * 在视觉范围内寻找任意一个满足条件的目标
     *
     * @param sprite    源精灵
     * @param condition 条件，满足该条件的精灵才可能被返回
     * @return 找到的目标精灵
     */
    public Optional<SpriteBo> findAnyTargetInSight(SpriteDo sprite, Predicate<SpriteDo> condition) {
        return spriteService.getOnlineSprites().values().stream()
                .filter(x -> isInSight(sprite, x.getX(), x.getY()))
                .filter(x -> !x.getId().equals(sprite.getId()))
                .filter(condition)
                .findAny();
    }

    /**
     * 在视觉范围内寻找最近的一个满足条件的目标
     *
     * @param sprite    源精灵
     * @param condition 条件，满足该条件的精灵才可能被返回
     * @return 找到的目标精灵
     */
    public Optional<SpriteBo> findNearestTargetInSight(SpriteDo sprite, Predicate<SpriteDo> condition) {
        return spriteService.getOnlineSprites().values().stream()
                .filter(x -> isInSight(sprite, x.getX(), x.getY()))
                .filter(x -> !x.getId().equals(sprite.getId()))
                .filter(condition)
                .min((x, y) -> (int) (calcDistance(sprite.getX(), sprite.getY(), x.getX(), x.getY()) - calcDistance(sprite.getX(), sprite.getY(), y.getX(), y.getY())));
    }

    /**
     * 在视觉范围内寻找所有的满足条件的目标
     */
    public List<SpriteBo> findAllTargetsInSight(SpriteDo sprite, Predicate<SpriteDo> condition) {
        return spriteService.getOnlineSprites().values().stream()
                .filter(x -> isInSight(sprite, x.getX(), x.getY()))
                .filter(x -> !x.getId().equals(sprite.getId()))
                .filter(condition)
                .toList();
    }

    /** 判断两个精灵是否接近（即快要碰撞） */
    public boolean isNear(SpriteDo sprite1, SpriteDo sprite2) {
        // 之所以这里不乘以widthRatio和heightRatio，是因为这里是检测是否接近而不是检测是否碰撞，因此放宽一点要求
        return Math.abs(sprite1.getX() - sprite2.getX()) < (sprite1.getWidth() + sprite2.getWidth()) / 2 &&
                Math.abs(sprite1.getY() - sprite2.getY()) < (sprite1.getHeight() + sprite2.getHeight()) / 2;
    }

    /** 精灵根据移动目标进行移动 */
    @Nullable
    public MoveVo move(SpriteBo sprite, MoveBo moveBo, MapBitsPermissionsBo permissions) {
        if (!moveBo.isMove()) {
            return null;
        }
        // 寻找路径
        List<Point> path = findPath(sprite, moveBo, permissions);
        // 如果路径为空，那么就不移动
        if (path.isEmpty()) {
            return null;
        }
        // 发送移动事件
        return new MoveVo(
                sprite.getId(),
                sprite.getSpeed() + sprite.getSpeedInc(),
                DataCompressor.compressPath(path),
                moveBo.getDestBuildingId(),
                moveBo.getDestSprite() == null ? null : moveBo.getDestSprite().getId(),
                moveBo.getDestSprite() == null ? null : GameCache.random.nextInt()
        );
    }
}
