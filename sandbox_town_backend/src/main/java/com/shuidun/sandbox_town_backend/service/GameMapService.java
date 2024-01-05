package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.mapper.GameMapMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 地图相关的服务
 */
@Slf4j
@Service
public class GameMapService {

    private final GameMapMapper gameMapMapper;

    private final SpriteService spriteService;

    @Value("${mapId}")
    private String mapId;

    public GameMapService(GameMapMapper gameMapMapper, SpriteService spriteService) {
        this.gameMapMapper = gameMapMapper;
        this.spriteService = spriteService;
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


    /**
     * 寻路算法
     *
     * @param initiator      发起者
     * @param x1             终点x坐标
     * @param y1             终点y坐标
     * @param destBuildingId 目标建筑物id，如果终点是建筑物，则传入建筑物id，否则传入null
     * @param destSprite     目标精灵，如果终点是精灵，则传入精灵信息，否则传入null
     * @return 路径节点列表，如果找不到路径，则返回空列表
     */
    public List<Point> findPath(SpriteWithTypeBo initiator, double x1, double y1,
                                @Nullable String destBuildingId, @Nullable SpriteWithTypeBo destSprite) {
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
        if (destSprite != null) {
            // 此时重点被修正为精灵中心点
            endX = (int) (destSprite.getX() / Constants.PIXELS_PER_GRID);
            endY = (int) (destSprite.getY() / Constants.PIXELS_PER_GRID);
            // 获取精灵的宽高
            double destSpriteWidth = destSprite.getWidth() * destSprite.getWidthRatio();
            double destSpriteHeight = destSprite.getHeight() * destSprite.getHeightRatio();
            // 将物品宽高的像素转换为地图坐标
            destSpriteHalfWidth = (int) Math.ceil(destSpriteWidth / Constants.PIXELS_PER_GRID) / 2;
            destSpriteHalfHeight = (int) Math.ceil(destSpriteHeight / Constants.PIXELS_PER_GRID) / 2;
        }
        // 调用寻路算法
        List<Point> path = PathUtils.findPath(GameCache.map, startX, startY, endX, endY, spriteHalfWidth, spriteHalfHeight, destBuildingHashCode, destSpriteHalfWidth, destSpriteHalfHeight);
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
    public List<Point> findPathNotTooClose(SpriteWithTypeBo initiator, double x1, double y1,
                                           @Nullable String destBuildingId, @Nullable SpriteWithTypeBo destSprite) {
        List<Point> path = findPath(initiator, x1, y1, destBuildingId, destSprite);
        // 去除后面一段
        int minLen = (int) (initiator.getWidth() * initiator.getWidthRatio() * 2.5 / Constants.PIXELS_PER_GRID);
        if (path.size() < minLen) {
            return Collections.emptyList();
        }
        // 去掉后面一段
        return path.subList(0, path.size() - minLen);
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
                if (GameCache.map[i][j] != 0) {
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
                    BufferedImage bufferedImage = GameCache.buildingTypesImages.get(building.getType());
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
                if (GameCache.map[i][j] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

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
                BufferedImage bufferedImage = GameCache.buildingTypesImages.get(building.getType());
                assert bufferedImage != null;
                // 获取当前格子中心在建筑物黑白图中的坐标
                int buildingPixelX = (int) (ratioX * bufferedImage.getWidth());
                int buildingPixelY = (int) (ratioY * bufferedImage.getHeight());
                // 获取当前格子中心的颜色
                int color = bufferedImage.getRGB(buildingPixelX, buildingPixelY);
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
        assert gameMap != null;
        return gameMap;
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
    public Pair<Double, Double> randomVelocity(SpriteDetailBo sprite) {
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

    /**
     * 在视觉范围内寻找所有的满足条件的目标
     */
    public List<SpriteDo> findAllTargetsInSight(SpriteDo sprite, Predicate<SpriteDo> condition) {
        return spriteService.getOnlineSprites().stream()
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

    /** 得到精灵合法的目标，即目标精灵必须存在，并且在线，并且在视野范围内。如果不合法，则返回null */
    public Optional<SpriteDo> getValidTarget(SpriteDo sprite) {
        // 如果精灵本身不在线
        if (sprite.getCache() == null) {
            return Optional.empty();
        }
        // 如果精灵的目标精灵不存在
        String targetSpriteId = sprite.getCache().getTargetSpriteId();
        if (targetSpriteId == null) {
            return Optional.empty();
        }
        // 如果目标精灵不在线
        SpriteDo targetSprite = spriteService.selectById(targetSpriteId);
        if (targetSprite == null || targetSprite.getCache() == null) {
            sprite.getCache().setTargetSpriteId(null);
            return Optional.empty();
        }
        // 如果目标精灵不在视野范围内
        if (!isInSight(sprite, targetSprite.getX(), targetSprite.getY())) {
            sprite.getCache().setTargetSpriteId(null);
            return Optional.empty();
        }
        return Optional.of(targetSprite);
    }

    /** 得到精灵合法的目标，并且以一定概率忘记目标 */
    public Optional<SpriteDo> getValidTargetWithRandomForget(SpriteDo sprite, double forgetProbability) {
        Optional<SpriteDo> targetSprite = getValidTarget(sprite);
        // 如果目标精灵不合法
        if (targetSprite.isEmpty()) {
            return Optional.empty();
        }
        // 以一定概率忘记目标
        if (GameCache.random.nextDouble() < forgetProbability) {
            if (sprite.getCache() != null) {
                sprite.getCache().setTargetSpriteId(null);
            }
            return Optional.empty();
        }
        return targetSprite;
    }

    /** 得到精灵的合法主人，即主人必须存在，并且在线，并且在视野范围内。如果不合法，则返回null */
    public Optional<SpriteDo> getValidOwner(SpriteDo sprite) {
        // 如果精灵本身不在线
        if (sprite.getCache() == null) {
            return Optional.empty();
        }
        // 如果精灵的主人不存在
        String owner = sprite.getOwner();
        if (owner == null) {
            return Optional.empty();
        }
        // 如果主人不在线
        SpriteDo ownerSprite = spriteService.selectById(owner);
        if (ownerSprite == null || ownerSprite.getCache() == null) {
            return Optional.empty();
        }
        // 如果主人不在视野范围内
        if (!isInSight(sprite, ownerSprite.getX(), ownerSprite.getY())) {
            return Optional.empty();
        }
        return Optional.of(ownerSprite);
    }
}
