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
        // 狗的处理函数
        typeToFunction.put(SpriteTypeEnum.DOG, sprite -> {
            // 获得狗的主人
            String owner = sprite.getOwner();
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
                // 如果狗有主人，那么狗一定概率就跟着主人走
                if (GameCache.random.nextDouble() < 0.6) {
                    return;
                }
                SpriteCache ownerSprite = GameCache.spriteCacheMap.get(owner);
                if (ownerSprite == null) {
                    return;
                }
                double distance = gameMapService.calcDistance(sprite.getX(), sprite.getY(), ownerSprite.getX(), ownerSprite.getY());
                // 如果距离过远（视野之外），那就不跟随
                if (distance > sprite.getVisionRange() + sprite.getVisionRangeInc()) {
                    return;
                }
                // 寻找路径
                var path = gameMapService.findPath(sprite, (int) ownerSprite.getX(), (int) ownerSprite.getY(), null, null);
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
                    spriteService.modifyLife(sprite.getId(), -1);
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
