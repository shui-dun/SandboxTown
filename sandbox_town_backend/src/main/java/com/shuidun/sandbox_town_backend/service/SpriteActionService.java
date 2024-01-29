package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 提供精灵Agent在决策和行动时所需的各种公共方法，例如寻找最近的目标
 * 该service依赖于spriteService、gameMapService等
 * 这是因为精灵的决策不仅取决于精灵本身的状态，还取决于地图周围的环境
 */
@Service
public class SpriteActionService {
    private final SpriteService spriteService;

    private final GameMapService gameMapService;


    public SpriteActionService(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
    }

    /** 计算两点之间的距离 */
    private double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /** 目标精灵是否在源精灵的视野内 */
    private boolean isInSight(SpriteDo source, double targetX, double targetY) {
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

    /** 精灵根据移动目标进行移动 */
    @Nullable
    public MoveVo move(SpriteDetailBo sprite, MoveBo moveBo, MapBitsPermissionsBo permissions) {
        if (!moveBo.isMove()) {
            return null;
        }
        // 寻找路径
        List<Point> path = gameMapService.findPath(sprite, moveBo, permissions);
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
