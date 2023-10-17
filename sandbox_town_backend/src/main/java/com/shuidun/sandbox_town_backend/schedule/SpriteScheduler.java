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

@Component
@Slf4j
public class SpriteScheduler {

    /** 类型到函数的映射 */
    private final Map<SpriteTypeEnum, Consumer<SpriteDo>> typeToFunction = new HashMap<>();

    private final SpriteService spriteService;

    public SpriteScheduler(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        // 玩家的处理函数
        typeToFunction.put(SpriteTypeEnum.USER, sprite -> {
            // 一定概率忘记目标（否则玩家的狗会一直追着攻击玩家的目标）
            if (GameCache.random.nextDouble() > 0.8) {
                sprite.getCache().setTargetSpriteId(null);
            }
        });
        // 狗的处理函数
        typeToFunction.put(SpriteTypeEnum.DOG, sprite -> {
            // 获得狗的主人
            String owner = sprite.getOwner();
            // 如果狗有目标精灵
            String targetId = sprite.getCache().getTargetSpriteId();
            if (targetId != null) {
                SpriteCache targetCache = GameCache.spriteCacheMap.get(targetId);
                // 如果目标精灵不存在，那就不跟随
                // 有一定概率即使目标精灵存在，也取消跟随目标
                if (targetCache == null || GameCache.random.nextDouble() > 0.8) {
                    sprite.getCache().setTargetSpriteId(null);
                    return;
                }
                double distance = gameMapService.calcDistance(sprite.getX(), sprite.getY(), targetCache.getX(), targetCache.getY());
                // 如果距离过远（视野之外），那就不跟随
                if (distance > sprite.getVisionRange() + sprite.getVisionRangeInc()) {
                    return;
                }
                // 寻找路径
                var path = gameMapService.findPath(sprite, targetCache.getX(), targetCache.getY(), null, targetId);
                // 如果找不到路径，那就不前往
                if (path == null) {
                    return;
                }
                // 发送移动消息
                WSMessageSender.sendResponse(new WSResponseVo(
                        WSResponseEnum.MOVE,
                        new MoveVo(
                                sprite.getId(),
                                sprite.getSpeed() + sprite.getSpeedInc(),
                                DataCompressor.compressPath(path),
                                null,
                                targetId
                        )
                ));
            } else {
                // 如果狗没有主人
                if (owner == null) {
                    // 随机移动
                    if (GameCache.random.nextDouble() < 0.5) {
                        return;
                    }
                    double randomVx = (sprite.getSpeed() + sprite.getSpeedInc()) * (Math.random() - 0.5);
                    double randomVy = (sprite.getSpeed() + sprite.getSpeedInc()) * (Math.random() - 0.5);
                    WSMessageSender.sendResponse(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(sprite.getId(), sprite.getX(), sprite.getY(), randomVx, randomVy)));
                } else {
                    // 如果狗有主人
                    SpriteCache ownerSprite = GameCache.spriteCacheMap.get(owner);
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
                    if (ownerTargetId != null && GameCache.spriteCacheMap.get(ownerTargetId) != null) {
                        sprite.getCache().setTargetSpriteId(ownerTargetId);
                    } else {
                        // 否则狗一定概率就跟着主人走
                        if (GameCache.random.nextDouble() < 0.6) {
                            return;
                        }
                        double distance = gameMapService.calcDistance(sprite.getX(), sprite.getY(), ownerSprite.getX(), ownerSprite.getY());
                        // 如果距离过远（视野之外），那就不跟随
                        if (distance > sprite.getVisionRange() + sprite.getVisionRangeInc()) {
                            return;
                        }
                        // 寻找路径
                        var path = gameMapService.findPath(sprite, ownerSprite.getX(), ownerSprite.getY(), null, null);
                        // 如果找不到路径，那就不跟随
                        if (path == null) {
                            return;
                        }
                        // 如果距离过近，那就不跟随，狗与主人不要离得太近
                        int minLen = (int) (sprite.getWidth() * sprite.getWidthRatio() * 2.5 / Constants.PIXELS_PER_GRID);
                        if (path.size() < minLen) {
                            return;
                        }
                        // 去掉后面一段
                        path = path.subList(0, path.size() - minLen);
                        // 发送移动消息
                        WSMessageSender.sendResponse(new WSResponseVo(WSResponseEnum.MOVE, new MoveVo(sprite.getId(), sprite.getSpeed() + sprite.getSpeedInc(), DataCompressor.compressPath(path), null, null)));
                    }
                }
            }
        });

    }

    private long counterOfSchedule = 0;

    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void schedule() {
        counterOfSchedule++;
        // 遍历所有角色
        for (String id : GameCache.spriteCacheMap.keySet()) {
            // 得到其角色
            SpriteDo sprite = spriteService.selectByIdWithDetail(id);
            if (sprite == null) {
                continue;
            }
            // 生命效果
            if (counterOfSchedule % 17 == 0) {
                if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE))) {
                    WSMessageSender.sendResponseList(spriteService.modifyLife(sprite.getId(), 1));
                }
            }
            // 烧伤效果
            if (counterOfSchedule % 5 == 0) {
                if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN))) {
                    WSMessageSender.sendResponseList(spriteService.modifyLife(sprite.getId(), -1));
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
            spriteService.reduceSpritesHunger(GameCache.spriteCacheMap.keySet(), 1);
        }
        // 恢复体力
        if (counterOfBatchSchedule % 13 == 0) {
            spriteService.recoverSpritesLife(GameCache.spriteCacheMap.keySet(), 80, 1);
        }

        if (counterOfBatchSchedule == Long.MAX_VALUE) {
            counterOfBatchSchedule = 0;
        }
    }
}
