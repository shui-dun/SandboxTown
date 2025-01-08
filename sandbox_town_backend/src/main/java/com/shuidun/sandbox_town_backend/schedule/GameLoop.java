package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.MoveVo;
import com.shuidun.sandbox_town_backend.bean.SpriteBo;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.service.MapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.service.TimeService;
import com.shuidun.sandbox_town_backend.utils.Concurrent;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 游戏循环
 */
@Component
@Slf4j
public class GameLoop {

    /** 游戏循环的时间间隔 */
    private final int GAME_LOOP_INTERVAL = 50;

    /** 帧率 */
    private final int FPS = 1000 / GAME_LOOP_INTERVAL;

    /** 上一帧的时间 */
    private long lastTime = System.currentTimeMillis();

    /** 执行一次生命效果的帧数 */
    private final int LIFE_FRAMES = 12 * FPS;

    /** 执行一次烧伤效果的帧率 */
    private final int BURN_FRAMES = 2 * FPS;

    /** 执行一次精灵行为的帧数 */
    private final int SPRITE_ACTION_FRAMES = FPS;

    /** 保存一次坐标的帧数 */
    private final int SAVE_COORDINATE_FRAMES = FPS;

    /** 减少饱腹值的帧数 */
    private final int REDUCE_HUNGER_FRAMES = 20 * FPS;

    /** 恢复体力的帧数 */
    private final int RECOVER_LIFE_FRAMES = 13 * FPS;

    /** 通知时间段的帧数 */
    private final int NOTIFY_TIME_FRAME_FRAMES = 5 * FPS;

    /** 使精灵缓存失效的帧数 */
    private final int INVALIDATE_CACHE_FRAMES = 60 * FPS;

    private final SpriteService spriteService;

    private final MapService mapService;

    private final EventHandler eventHandler;

    private final TimeService timeService;

    /** 当前帧数 */
    private long curFrame = 0;

    public GameLoop(MapService mapService, SpriteService spriteService, EventHandler eventHandler, TimeService timeService) throws InterruptedException {
        this.spriteService = spriteService;
        this.mapService = mapService;
        this.eventHandler = eventHandler;
        this.timeService = timeService;
        // 初始化地图
        mapService.init();
    }

    @Scheduled(initialDelay = 0, fixedDelay = GAME_LOOP_INTERVAL)
    public void gameLoop() {
        try {
            curFrame++;

            var time = System.currentTimeMillis();
            var diff = time - lastTime;
            // log.info("curFrame: {}, diff: {}", curFrame, diff);
            if (diff > GAME_LOOP_INTERVAL * 2) {
                log.info("time diff between two frames is too large: {}", diff);
            }
            // 定期使缓存失效
            List<SpriteBo> sprites = spriteService.getOnlineSpritesByFrame(INVALIDATE_CACHE_FRAMES, curFrame);
            for (SpriteBo sprite : sprites) {
                spriteService.invalidateSpriteCache(sprite.getId());
            }
            // 处理事件（用户输入）
            eventHandler.handleMessages();
            // 精灵交互行为（由于相互影响，无法并行处理）
            for (SpriteBo spriteBo : spriteService.getOnlineSprites().values()) {
                if (spriteBo.getInteractSpriteId() == null) {
                    continue;
                }
                SpriteBo targetSprite = spriteService.selectOnlineById(spriteBo.getInteractSpriteId());
                spriteBo.setInteractSpriteId(null);
                if (targetSprite == null) {
                    continue;
                }
                WSMessageSender.addResponses(spriteService.interact(spriteBo, targetSprite));
            }
            // 精灵决策
            sprites = spriteService.getOnlineSpritesByFrame(SPRITE_ACTION_FRAMES, curFrame);
            sprites.addAll(spriteService.onlineUsers());
            Concurrent.executeInThreadPool(sprites, (sprite) -> {
                MoveVo moveVo = spriteService.move(sprite);
                if (moveVo != null) {
                    WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, moveVo));
                }
            });
            // 处理被动效果
            // 生命效果
            sprites = spriteService.getOnlineSpritesByFrame(LIFE_FRAMES, curFrame).stream()
                    .filter(sprite -> spriteService.hasEffect(sprite.getId(), EffectEnum.LIFE))
                    .toList();
            Concurrent.executeInThreadPool(sprites, (sprite) -> WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1)));
            // 烧伤效果
            sprites = spriteService.getOnlineSpritesByFrame(BURN_FRAMES, curFrame).stream()
                    .filter(sprite -> spriteService.hasEffect(sprite.getId(), EffectEnum.BURN))
                    .toList();
            Concurrent.executeInThreadPool(sprites, (sprite) -> WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1)));
            // 减少饱腹值
            sprites = spriteService.getOnlineSpritesByFrame(REDUCE_HUNGER_FRAMES, curFrame);
            spriteService.reduceSpritesHunger(sprites);
            // 恢复体力
            sprites = spriteService.getOnlineSpritesByFrame(RECOVER_LIFE_FRAMES, curFrame);
            WSMessageSender.addResponses(spriteService.recoverSpritesLife(sprites));
            // 更新时间
            if (time > timeService.getTimeFrame().getTimeFrameEndTime()) {
                switch (timeService.getTimeFrame().getTimeFrame()) {
                    case DAY:
                        timeService.enterDusk();
                        break;
                    case DUSK:
                        timeService.enterNight();
                        break;
                    case NIGHT:
                        timeService.enterDawn();
                        break;
                    case DAWN:
                        timeService.enterDay();
                        break;
                }
            }
            // 通知时间段
            if (curFrame % NOTIFY_TIME_FRAME_FRAMES == 0) {
                timeService.notifyTimeFrame();
            }

            lastTime = time;
        } catch (Exception e) {
            log.error("GameLoop error", e);
        }

    }
}
