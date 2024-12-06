package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.agent.SpriteAgent;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.*;
import com.shuidun.sandbox_town_backend.utils.Concurrent;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 游戏初始化器
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
    private final int SPRITE_ACTION_FRAMES = 1 * FPS;

    /** 保存一次坐标的帧数 */
    private final int SAVE_COORDINATE_FRAMES = 1 * FPS;

    /** 减少饱腹值的帧数 */
    private final int REDUCE_HUNGER_FRAMES = 20 * FPS;

    /** 恢复体力的帧数 */
    private final int RECOVER_LIFE_FRAMES = 13 * FPS;

    /** 通知时间段的帧数 */
    private final int NOTIFY_TIME_FRAME_FRAMES = 5 * FPS;

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    private final SpriteActionService spriteActionService;

    private final EventHandler eventHandler;

    private final TimeService timeService;

    /** 精灵类型到精灵Agent的映射 */
    private final Map<SpriteTypeEnum, SpriteAgent> typeToAgent = new HashMap<>();

    /** 当前帧数 */
    private long curFrame = 0;

    public GameLoop(List<SpriteAgent> spriteAgents, GameMapService gameMapService, SpriteActionService spriteActionService, SpriteService spriteService, EcosystemService ecosystemService, EventHandler eventHandler, TimeService timeService) throws InterruptedException {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
        this.spriteActionService = spriteActionService;
        this.eventHandler = eventHandler;
        this.timeService = timeService;
        for (SpriteAgent agent : spriteAgents) {
            typeToAgent.put(agent.getType(), agent);
        }

        // 获得地图信息
        GameMapDo gameMap = gameMapService.getGameMap();

        // 设置随机数种子
        GameCache.random.setSeed(gameMap.getSeed());

        // 初始化地图
        GameCache.map = new int[gameMap.getWidth() / Constants.PIXELS_PER_GRID][gameMap.getHeight() / Constants.PIXELS_PER_GRID];
        GameCache.buildingsHashCodeMap = new int[gameMap.getWidth() / Constants.PIXELS_PER_GRID][gameMap.getHeight() / Constants.PIXELS_PER_GRID];

        // 在地图上生成围墙
        gameMapService.generateMaze(GameCache.map, 0, 0, GameCache.map.length / 2, GameCache.map[0].length / 2);

        // 在地图上放置建筑
        boolean containsBuilding = gameMapService.placeAllBuildingsOnMap();

        // 放置没有主人的角色
        spriteService.getUnownedSprites().forEach(sprite ->
                spriteService.online(sprite.getId())
        );

        // 如果没有建筑物，则生成一定数量的建筑物
        if (!containsBuilding) {
            ecosystemService.createEnvironment(gameMap.getWidth() * gameMap.getHeight() / 300000);
        }
    }

    @Scheduled(initialDelay = 0, fixedDelay = GAME_LOOP_INTERVAL)
    public void gameLoop() {
        try {
            curFrame++;

            var time = System.currentTimeMillis();
            log.info("time diff between two frames: {}", time - lastTime);

            // 处理事件
            eventHandler.handleMessages();

            // 生命效果
            List<SpriteDetailBo> sprites = spriteService.getOnlineSpritesWithDetailByFrame(LIFE_FRAMES, curFrame).stream()
                    .filter(sprite -> sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE)))
                    .toList();
            Concurrent.executeInThreadPool(sprites, (sprite) -> WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1)));
            // 烧伤效果
            sprites = spriteService.getOnlineSpritesWithDetailByFrame(BURN_FRAMES, curFrame).stream()
                    .filter(sprite -> sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN)))
                    .toList();
            Concurrent.executeInThreadPool(sprites, (sprite) -> WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1)));
            // 调用精灵行为
            sprites = spriteService.getOnlineSpritesWithDetailByFrame(SPRITE_ACTION_FRAMES, curFrame);
            Concurrent.executeInThreadPool(sprites, (sprite) -> {
                var agent = typeToAgent.get(sprite.getType());
                if (agent != null) {
                    MoveBo moveBo = agent.act(sprite);
                    MoveVo moveVo = spriteActionService.move(sprite, moveBo, agent.mapBitsPermissions(sprite));
                    if (moveVo == null) {
                        return;
                    }
                    WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, moveVo));
                }
            });
            // 保存坐标
            sprites = spriteService.getOnlineSpritesWithDetailByFrame(SAVE_COORDINATE_FRAMES, curFrame);
            Concurrent.executeInThreadPool(sprites, (sprite) -> spriteService.updatePosition(sprite.getId(), sprite.getX(), sprite.getY()));
            // 减少饱腹值
            if (curFrame % REDUCE_HUNGER_FRAMES == 0) {
                spriteService.reduceSpritesHunger(spriteService.getOnlineSpritesCache().keySet(), 1);
            }
            // 恢复体力
            if (curFrame % RECOVER_LIFE_FRAMES == 0) {
                spriteService.recoverSpritesLife(spriteService.getOnlineSpritesCache().keySet(), 1);
            }
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
