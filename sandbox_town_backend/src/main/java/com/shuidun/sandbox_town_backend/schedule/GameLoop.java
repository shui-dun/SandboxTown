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
import lombok.extern.slf4j.Slf4j;
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

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    private final SpriteActionService spriteActionService;

    /** 精灵类型到精灵Agent的映射 */
    private final Map<SpriteTypeEnum, SpriteAgent> typeToAgent = new HashMap<>();

    private long counter = 0;

    public GameLoop(List<SpriteAgent> spriteAgents, GameMapService gameMapService, SpriteActionService spriteActionService, SpriteService spriteService, EcosystemService ecosystemService) throws InterruptedException {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
        this.spriteActionService = spriteActionService;
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

        // 主游戏循环
        // 必须在一个新线程中运行，否则会阻塞Spring的初始化
        new Thread(this::gameLoop).start();
    }

    private void gameLoop() {
        while (true) {
            try {
                Thread.sleep(Constants.GAME_LOOP_INTERVAL);
                counter++;

                // 遍历所有角色
                for (String id : spriteService.getOnlineSpritesCache().keySet()) {
                    // 得到其角色
                    SpriteDetailBo sprite = spriteService.selectByIdWithDetail(id);
                    // 如果精灵不存在或者不在线，就不处理
                    if (sprite == null || sprite.getCache() == null) {
                        continue;
                    }
                    // 生命效果
                    if (counter % Constants.EFFECT_FRAMES == 0) {
                        if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE))) {
                            WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1));
                        }
                    }
                    // 烧伤效果
                    if (counter % Constants.BURN_FRAMES == 0) {
                        if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN))) {
                            WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1));
                        }
                    }
                    // 调用精灵行为
                    if (counter % Constants.SPRITE_ACTION_FRAMES == 0) {
                        var agent = typeToAgent.get(sprite.getType());
                        if (agent != null) {
                            MoveBo moveBo = agent.act(sprite);
                            MoveVo moveVo = spriteActionService.move(sprite, moveBo, agent.mapBitsPermissions(sprite));
                            if (moveVo == null) {
                                continue;
                            }
                            WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, moveVo));
                        }
                    }
                    // 保存坐标
                    if (counter % Constants.SAVE_COORDINATE_FRAMES == 0) {
                        spriteService.updatePosition(sprite.getId(), sprite.getX(), sprite.getY());
                    }
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
    }
}
