package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import com.shuidun.sandbox_town_backend.mapper.GameMapMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import com.shuidun.sandbox_town_backend.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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

    /** 画一个2x2的墙 */
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

    /** 将所有建筑物放置在地图上 */
    public void placeAllBuildingsOnMap() {
        // 建筑物的黑白图的字典
        var buildingTypes = buildingTypeMapper.selectList(null);
        for (BuildingTypeDo buildingType : buildingTypes) {
            BuildingTypeEnum buildingTypeId = buildingType.getId();
            String imagePath = buildingType.getImagePath();
            try {
                BufferedImage image = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
                GameCache.buildingTypesImages.put(buildingTypeId, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 获取当前地图上的所有建筑物
        var buildings = buildingMapper.selectByMapId(mapId);

        // 将建筑放置在地图上
        for (BuildingDo building : buildings) {
            placeBuildingOnMap(building);
        }
    }


    /**
     * 寻路算法
     *
     * @param initiator      发起者
     * @param x1             终点x坐标
     * @param y1             终点y坐标
     * @param destBuildingId 目标建筑物id，如果终点是建筑物，则传入建筑物id，否则传入null
     * @param destSpriteId   目标精灵id，如果终点是精灵，则传入精灵id，否则传入null
     */
    public List<Point> findPath(SpriteDo initiator, double x1, double y1,
                                String destBuildingId, String destSpriteId) {
        double x0 = initiator.getX();
        double y0 = initiator.getY();
        // 将物理坐标转换为地图坐标
        int startX = (int) Math.round(x0) / Constants.PIXELS_PER_GRID;
        int startY = (int) Math.round(y0) / Constants.PIXELS_PER_GRID;
        int endX = (int) Math.round(x1) / Constants.PIXELS_PER_GRID;
        int endY = (int) Math.round(y1) / Constants.PIXELS_PER_GRID;
        double spriteWidth = initiator.getWidth() * initiator.getWidthRatio();
        double spriteHeight = initiator.getHeight() * initiator.getHeightRatio();
        // 将物品宽高的像素转换为地图坐标
        int spriteHalfWidth = (int) Math.ceil(spriteWidth / Constants.PIXELS_PER_GRID) / 2;
        int spriteHalfHeight = (int) Math.ceil(spriteHeight / Constants.PIXELS_PER_GRID) / 2;
        // 如果目标是建筑物
        Integer destBuildingHashCode = destBuildingId == null ? null : destBuildingId.hashCode();
        // 如果目标是精灵
        Integer destSpriteHalfWidth = null;
        Integer destSpriteHalfHeight = null;
        if (destSpriteId != null) {
            // 此时重点被修正为精灵中心点
            endX = (int) (GameCache.spriteCacheMap.get(destSpriteId).getX() / Constants.PIXELS_PER_GRID);
            endY = (int) (GameCache.spriteCacheMap.get(destSpriteId).getY() / Constants.PIXELS_PER_GRID);
            // 获取精灵的宽高
            SpriteDo destSprite = spriteService.selectByIdWithType(destSpriteId);
            double destSpriteWidth = destSprite.getWidth() * destSprite.getWidthRatio();
            double destSpriteHeight = destSprite.getHeight() * destSprite.getHeightRatio();
            // 将物品宽高的像素转换为地图坐标
            destSpriteHalfWidth = (int) Math.ceil(destSpriteWidth / Constants.PIXELS_PER_GRID) / 2;
            destSpriteHalfHeight = (int) Math.ceil(destSpriteHeight / Constants.PIXELS_PER_GRID) / 2;
        }
        // 调用寻路算法
        List<Point> path = PathUtils.findPath(GameCache.map, startX, startY, endX, endY, spriteHalfWidth, spriteHalfHeight, destBuildingHashCode, destSpriteHalfWidth, destSpriteHalfHeight);
        // 判断是否为空
        if (path == null) {
            return null;
        }
        // 将地图坐标转换为物理坐标
        // 一般来说，地图坐标是整数，而物理坐标是浮点数
        // 但是显然在这里计算得到的物理坐标也是整数
        for (Point point : path) {
            point.setX(point.getX() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
            point.setY(point.getY() * Constants.PIXELS_PER_GRID + Constants.PIXELS_PER_GRID / 2);
        }
        return path;
    }

    /** 找到路径，但与目标保持一定距离 */
    public List<Point> findPathNotTooClose(SpriteDo initiator, double x1, double y1,
                                           String destBuildingId, String destSpriteId) {
        List<Point> path = findPath(initiator, x1, y1, destBuildingId, destSpriteId);
        if (path == null) {
            return null;
        }
        // 如果距离过近，那就不跟随，狗与主人不要离得太近
        int minLen = (int) (initiator.getWidth() * initiator.getWidthRatio() * 2.5 / Constants.PIXELS_PER_GRID);
        if (path.size() < minLen) {
            return null;
        }
        // 去掉后面一段
        return path.subList(0, path.size() - minLen);
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
                if (GameCache.map[i][j] != 0) {
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

    /** 放置建筑 */
    private void placeBuildingOnMap(BuildingDo building) {
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

    /** 得到地图信息 */
    public GameMapDo getGameMap() {
        GameMapDo gameMap = gameMapMapper.selectById(mapId);
        gameMap.setData(GameCache.map);
        return gameMap;
    }

    /** 初始化地图（建造建筑等） */
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
            double x = Math.random() * (GameCache.map.length - 8) * Constants.PIXELS_PER_GRID;
            double y = Math.random() * (GameCache.map[0].length - 8) * Constants.PIXELS_PER_GRID;
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
            if (!isBuildingOverlap(building)) {
                // 如果不重叠，添加建筑到数据库
                buildingMapper.insert(building);
                // 放置建筑
                placeBuildingOnMap(building);
                // 如果是树
                if (buildingType.getId().equals(BuildingTypeEnum.TREE)) {
                    treeService.createRandomTree(building.getId());
                } else if (buildingType.getId().equals(BuildingTypeEnum.STORE)) { // 如果是商店
                    // 刷新商店商品
                    storeService.refresh(building.getId());
                }
            } else {
                log.info("建筑重叠，重新生成建筑");
                // 如果重叠，重新生成建筑
                --i;
            }
        }
        // 生成精灵
        spriteService.refreshSprites(TimeFrameEnum.DAY);
        spriteService.refreshSprites(TimeFrameEnum.DUSK);
        spriteService.refreshSprites(TimeFrameEnum.NIGHT);
        spriteService.refreshSprites(TimeFrameEnum.DAWN);
    }

    /** 计算两点之间的距离 */
    public double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /** 目标精灵是否在源精灵的视野内 */
    public boolean isInSight(SpriteDo source, double targetX, double targetY) {
        return calcDistance(source.getX(), source.getY(), targetX, targetY) <= source.getVisionRange() + source.getVisionRange();
    }

    /** 得到随机移动速度 */
    public Pair<Double, Double> randomVelocity(SpriteDo sprite) {
        double coefficient = 0.9;
        double randomVx = coefficient * (sprite.getSpeed() + sprite.getSpeedInc()) * (Math.random() - 0.5);
        double randomVy = coefficient * (sprite.getSpeed() + sprite.getSpeedInc()) * (Math.random() - 0.5);
        return Pair.of(randomVx, randomVy);
    }

    /**
     * 在视觉范围内寻找任意一个满足条件的目标
     *
     * @param sprite    源精灵
     * @param condition 条件，满足该条件的精灵才可能被返回
     * @return 找到的目标精灵
     */
    public Optional<SpriteDo> findAnyTargetInSight(SpriteDo sprite, Predicate<SpriteDo> condition) {
        return spriteService.getOnlineSprites().stream()
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
    public Optional<SpriteDo> findNearestTargetInSight(SpriteDo sprite, Predicate<SpriteDo> condition) {
        return spriteService.getOnlineSprites().stream()
                .filter(x -> isInSight(sprite, x.getX(), x.getY()))
                .filter(x -> !x.getId().equals(sprite.getId()))
                .filter(condition)
                .min((x, y) -> (int) (calcDistance(sprite.getX(), sprite.getY(), x.getX(), x.getY()) - calcDistance(sprite.getX(), sprite.getY(), y.getX(), y.getY())));
    }

    /** 判断两个精灵是否接近（即快要碰撞） */
    public boolean isNear(SpriteDo sprite1, SpriteDo sprite2) {
        // 之所以这里不乘以widthRatio和heightRatio，是因为这里是检测是否接近而不是检测是否碰撞，因此放宽一点要求
        return Math.abs(sprite1.getX() - sprite2.getX()) < (sprite1.getWidth() + sprite2.getWidth()) / 2 &&
                Math.abs(sprite1.getY() - sprite2.getY()) < (sprite1.getHeight() + sprite2.getHeight()) / 2;
    }
}
