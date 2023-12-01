package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Component
@Slf4j
public class SpriteScheduler {

    /** 类型到函数的映射 */
    private final Map<SpriteTypeEnum, Consumer<SpriteDetailBo>> typeToFunction = new HashMap<>();

    private final SpriteService spriteService;

    public SpriteScheduler(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        // 玩家的处理函数
        typeToFunction.put(SpriteTypeEnum.USER, sprite -> {
            assert sprite.getCache() != null;
            // 一定概率忘记目标（否则玩家的狗会一直追着攻击玩家的目标）
            if (GameCache.random.nextDouble() > 0.8) {
                sprite.getCache().setTargetSpriteId(null);
            }
        });
        // 狗的处理函数
        typeToFunction.put(SpriteTypeEnum.DOG, sprite -> {
            assert sprite.getCache() != null;
            // 获得狗的主人
            String owner = sprite.getOwner();
            // 如果狗有目标精灵
            String targetId = sprite.getCache().getTargetSpriteId();
            if (targetId != null) {
                SpriteDetailBo target = spriteService.selectByIdWithDetail(targetId);
                // 如果目标精灵不存在或者不在线，那就不跟随
                // 有一定概率即使目标精灵存在，也取消跟随目标
                if (target == null
                        || target.getCache() == null
                        || GameCache.random.nextDouble() > 0.8) {
                    sprite.getCache().setTargetSpriteId(null);
                    return;
                }
                // 如果距离过远（视野之外），那就不跟随
                if (!gameMapService.isInSight(sprite, target.getX(), target.getY())) {
                    return;
                }
                // 寻找路径
                var path = gameMapService.findPath(sprite, target.getX(), target.getY(), null, target);
                // 如果找不到路径，那就不前往
                if (path.isEmpty()) {
                    return;
                }
                // 发送移动消息
                WSMessageSender.addResponse(new WSResponseVo(
                        WSResponseEnum.MOVE,
                        new MoveVo(
                                sprite.getId(),
                                sprite.getSpeed() + sprite.getSpeedInc(),
                                DataCompressor.compressPath(path),
                                null,
                                targetId,
                                GameCache.random.nextInt()
                        )
                ));
            } else {
                // 如果狗没有主人
                if (owner == null) {
                    // 随机移动
                    if (GameCache.random.nextDouble() < 0.5) {
                        return;
                    }
                    var randomVelocity = gameMapService.randomVelocity(sprite);
                    WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(sprite.getId(), sprite.getX(), sprite.getY(), randomVelocity.getFirst(), randomVelocity.getSecond())));
                } else {
                    // 如果狗的主人在线
                    SpriteCache ownerSprite = spriteService.getSpriteCache(owner);
                    if (ownerSprite == null) {
                        return;
                    }
                    // 如果主人有攻击目标，那么狗也以主人的攻击目标为攻击目标
                    // 这里有一个有趣的特例：如果主人的攻击目标是狗，那么狗可能会自残
                    // 具体触发条件如下：
                    // 1. 主人攻击它养的狗a
                    // 2. 于是狗a会攻击主人
                    // 3. 主人的另一只狗狗b会攻击狗a
                    // 4. 于是狗a和狗b相互攻击
                    // 5. 如果狗a杀死了狗b，那么狗a接着可能会攻击自己
                    String ownerTargetId = ownerSprite.getTargetSpriteId();
                    if (ownerTargetId != null && spriteService.getSpriteCache(ownerTargetId) != null) {
                        sprite.getCache().setTargetSpriteId(ownerTargetId);
                    } else {
                        // 否则狗一定概率就跟着主人走
                        if (GameCache.random.nextDouble() < 0.6) {
                            return;
                        }
                        // 如果距离过远（视野之外），那就不跟随
                        if (!gameMapService.isInSight(sprite, ownerSprite.getX(), ownerSprite.getY())) {
                            return;
                        }
                        // 寻找路径，但保持一定距离
                        var path = gameMapService.findPathNotTooClose(sprite, ownerSprite.getX(), ownerSprite.getY(), null, null);
                        // 如果找不到路径，那就不跟随
                        if (path.isEmpty()) {
                            return;
                        }
                        // 发送移动消息
                        WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, new MoveVo(sprite.getId(), sprite.getSpeed() + sprite.getSpeedInc(), DataCompressor.compressPath(path), null, null, null)));
                    }
                }
            }
        });
        // 蜘蛛的处理逻辑
        typeToFunction.put(SpriteTypeEnum.SPIDER, sprite -> {
            assert sprite.getCache() != null;
            // 在视觉范围内寻找一个目标
            // 蜘蛛的攻击目标需要满足的条件（必须有主人，并且不是蜘蛛）
            Predicate<SpriteDo> condition = (s) -> s.getType() != SpriteTypeEnum.SPIDER
                    && (s.getOwner() != null || s.getType() == SpriteTypeEnum.USER);
            String finalTargetId;
            String originalTargetId = sprite.getCache().getTargetSpriteId();
            SpriteCache originalTarget = originalTargetId == null ? null : spriteService.getSpriteCache(originalTargetId);
            // 如果蜘蛛没有目标，或者目标已经不存在，或者目标不在线
            if (originalTargetId == null
                    || originalTarget == null) {
                finalTargetId = gameMapService.findAnyTargetInSight(sprite, condition).map(SpriteDo::getId).orElse(null);
            } else {
                // 判断目标是否过远
                if (gameMapService.isInSight(sprite, originalTarget.getX(), originalTarget.getY())) {
                    // 有一定概率忘记目标
                    if (GameCache.random.nextDouble() > 0.9) {
                        finalTargetId = gameMapService.findAnyTargetInSight(sprite, condition).map(SpriteDo::getId).orElse(null);
                    } else {
                        finalTargetId = originalTargetId;
                    }
                } else {
                    finalTargetId = gameMapService.findAnyTargetInSight(sprite, condition).map(SpriteDo::getId).orElse(null);
                }
            }
            SpriteWithTypeBo finalTarget = finalTargetId == null ? null
                    : spriteService.selectByIdWithType(finalTargetId);
            if (finalTarget == null) {
                // 随机移动
                if (GameCache.random.nextDouble() < 0.7) {
                    return;
                }
                var randomVelocity = gameMapService.randomVelocity(sprite);
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(sprite.getId(), sprite.getX(), sprite.getY(), randomVelocity.getFirst(), randomVelocity.getSecond())));
                return;
            }
            sprite.getCache().setTargetSpriteId(finalTargetId);
            // 寻找路径
            var path = gameMapService.findPath(sprite, finalTarget.getX(), finalTarget.getY(), null, finalTarget);
            // 如果找不到路径，那就不前往
            if (path.isEmpty()) {
                return;
            }
            // 发送移动消息
            WSMessageSender.addResponse(new WSResponseVo(
                    WSResponseEnum.MOVE,
                    new MoveVo(
                            sprite.getId(),
                            sprite.getSpeed() + sprite.getSpeedInc(),
                            DataCompressor.compressPath(path),
                            null,
                            finalTargetId,
                            GameCache.random.nextInt()
                    )
            ));
        });
    }

    private long counterOfSchedule = 0;

    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void schedule() {
        counterOfSchedule++;
        // 遍历所有角色
        for (String id : spriteService.getOnlineSpritesCache().keySet()) {
            // 得到其角色
            SpriteDetailBo sprite = spriteService.selectByIdWithDetail(id);
            // 如果精灵不存在或者不在线，就不处理
            if (sprite == null || sprite.getCache() == null) {
                continue;
            }
            // 生命效果
            if (counterOfSchedule % 12 == 0) {
                if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE))) {
                    WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1));
                }
            }
            // 烧伤效果
            if (counterOfSchedule % 2 == 0) {
                if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN))) {
                    WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1));
                }
            }
            // 调用对应的处理函数
            var func = typeToFunction.get(sprite.getType());
            if (func != null) {
                func.accept(sprite);
            }
            // 保存坐标
            spriteService.updatePosition(sprite.getId(), sprite.getX(), sprite.getY());
        }

        // 其实当计数器重置时，会导致所有这些定时任务的执行时间都会不准确
        // 但是这个问题不大，因为Long.MAX_VALUE是一个很大的数，在有限的时间内不会重置
        if (counterOfSchedule == Long.MAX_VALUE) {
            counterOfSchedule = 0;
        }
    }

    private long counterOfBatchSchedule = 0;

    @Scheduled(initialDelay = 500, fixedDelay = 1000)
    public void batchSchedule() {
        counterOfBatchSchedule++;
        // 减少饱腹值
        if (counterOfBatchSchedule % 20 == 0) {
            spriteService.reduceSpritesHunger(spriteService.getOnlineSpritesCache().keySet(), 1);
        }
        // 恢复体力
        if (counterOfBatchSchedule % 13 == 0) {
            spriteService.recoverSpritesLife(spriteService.getOnlineSpritesCache().keySet(), Constants.HUNGER_THRESHOLD, 1);
        }

        if (counterOfBatchSchedule == Long.MAX_VALUE) {
            counterOfBatchSchedule = 0;
        }
    }
}
