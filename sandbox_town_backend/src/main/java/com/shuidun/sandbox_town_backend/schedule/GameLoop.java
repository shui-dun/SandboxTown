package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.agent.SpriteAgent;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.EcosystemService;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteActionService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import com.shuidun.sandbox_town_backend.websocket.WSRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;


/**
 * 游戏初始化器
 */
@Component
@Slf4j
public class GameLoop {

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    private final SpriteActionService spriteActionService;

    private final WSRequestHandler wsRequestHandler;

    /** 精灵类型到精灵Agent的映射 */
    private final Map<SpriteTypeEnum, SpriteAgent> typeToAgent = new HashMap<>();

    private long counter = 0;

    public GameLoop(List<SpriteAgent> spriteAgents, GameMapService gameMapService, SpriteActionService spriteActionService, SpriteService spriteService, EcosystemService ecosystemService, WSRequestHandler wsRequestHandler) throws InterruptedException {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
        this.spriteActionService = spriteActionService;
        this.wsRequestHandler = wsRequestHandler;
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
            counter++;

            var time = System.currentTimeMillis();
            log.info("time diff between two frames: {}", time - lastTime);
            lastTime = time;

            // 处理事件
            wsRequestHandler.handleMessages();

            // 生命效果
            if (counter % Constants.EFFECT_FRAMES == 0) {
                List<SpriteDetailBo> sprites = spriteService.getOnlineSpritesWithDetail().stream()
                        .filter(sprite -> sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE)))
                        .toList();
                executeInThreadPool(sprites, (sprite) -> {
                    WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1));
                });
            }
            // 烧伤效果
            if (counter % Constants.BURN_FRAMES == 0) {
                List<SpriteDetailBo> sprites = spriteService.getOnlineSpritesWithDetail().stream()
                        .filter(sprite -> sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN)))
                        .toList();
                executeInThreadPool(sprites, (sprite) -> {
                    WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1));
                });
            }
            // 调用精灵行为
            if (counter % Constants.SPRITE_ACTION_FRAMES == 0) {
                List<SpriteDetailBo> sprites = spriteService.getOnlineSpritesWithDetail();
                executeInThreadPool(sprites, (sprite) -> {
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
            }
            // 保存坐标
            if (counter % Constants.SAVE_COORDINATE_FRAMES == 0) {
                List<SpriteDetailBo> sprites = spriteService.getOnlineSpritesWithDetail();
                executeInThreadPool(sprites, (sprite) -> {
                    spriteService.updatePosition(sprite.getId(), sprite.getX(), sprite.getY());
                });
            }
            // 减少饱腹值
            if (counter % Constants.REDUCE_HUNGER_FRAMES == 0) {
                spriteService.reduceSpritesHunger(spriteService.getOnlineSpritesCache().keySet(), 1);
            }
            // 恢复体力
            if (counter % Constants.RECOVER_LIFE_FRAMES == 0) {
                spriteService.recoverSpritesLife(spriteService.getOnlineSpritesCache().keySet(), Constants.HUNGER_THRESHOLD, 1);
            }
        } catch (Exception e) {
            log.error("GameLoop error", e);
        }

    }

    // 在线程池中执行任务
    private <T> void executeInThreadPool(List<T> items, Consumer<T> consumer) {
        List<CompletableFuture<Void>> futures = items.stream()
                .map(item -> CompletableFuture.runAsync(() -> consumer.accept(item), GameCache.executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
