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

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 游戏初始化器
 */
@Component
@Slf4j
public class GameLoop {

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

        // 初始化线程池
        GameCache.executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(), // 核心线程数
                Runtime.getRuntime().availableProcessors(), // 最大线程数
                60L, // 空闲线程存活时间
                java.util.concurrent.TimeUnit.SECONDS, // 时间单位
                new LinkedBlockingQueue<>(100) // 阻塞队列
        );


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

    private long lastTime = System.currentTimeMillis();

    @Scheduled(initialDelay = 0, fixedDelay = Constants.GAME_LOOP_INTERVAL)
    public void gameLoop() {
        try {
            curFrame++;

            var time = System.currentTimeMillis();
            log.info("time diff between two frames: {}", time - lastTime);

            // 处理事件
            eventHandler.handleMessages();

            // 生命效果
            List<SpriteDetailBo> sprites = spriteService.getOnlineSpritesWithDetailByFrame(Constants.LIFE_FRAMES, curFrame).stream()
                    .filter(sprite -> sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE)))
                    .toList();
            Concurrent.executeInThreadPool(sprites, (sprite) -> {
                WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1));
            });
            // 烧伤效果
            sprites = spriteService.getOnlineSpritesWithDetailByFrame(Constants.BURN_FRAMES, curFrame).stream()
                    .filter(sprite -> sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN)))
                    .toList();
            Concurrent.executeInThreadPool(sprites, (sprite) -> {
                WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1));
            });
            // 调用精灵行为
            sprites = spriteService.getOnlineSpritesWithDetailByFrame(Constants.SPRITE_ACTION_FRAMES, curFrame);
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
            sprites = spriteService.getOnlineSpritesWithDetailByFrame(Constants.SAVE_COORDINATE_FRAMES, curFrame);
            Concurrent.executeInThreadPool(sprites, (sprite) -> {
                spriteService.updatePosition(sprite.getId(), sprite.getX(), sprite.getY());
            });
            // 减少饱腹值
            if (curFrame % Constants.REDUCE_HUNGER_FRAMES == 0) {
                spriteService.reduceSpritesHunger(spriteService.getOnlineSpritesCache().keySet(), 1);
            }
            // 恢复体力
            if (curFrame % Constants.RECOVER_LIFE_FRAMES == 0) {
                spriteService.recoverSpritesLife(spriteService.getOnlineSpritesCache().keySet(), Constants.HUNGER_THRESHOLD, 1);
            }
            // 更新时间
            if (time > GameCache.timeFrame.getTimeFrameEndTime()) {
                switch (GameCache.timeFrame.getTimeFrame()) {
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
            if (curFrame % Constants.NOTIFY_TIME_FRAME_FRAMES == 0) {
                timeService.notifyTimeFrame();
            }

            lastTime = time;
        } catch (Exception e) {
            log.error("GameLoop error", e);
        }

    }
}
