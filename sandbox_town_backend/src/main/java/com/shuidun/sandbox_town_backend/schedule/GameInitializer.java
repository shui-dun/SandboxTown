package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.GameMapDo;
import com.shuidun.sandbox_town_backend.bean.SpriteCache;
import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 游戏初始化器
 */
@Component
@Slf4j
public class GameInitializer {

    public GameInitializer(GameMapService gameMapService, SpriteService spriteService) {

        // 获得地图信息
        GameMapDo gameMap = gameMapService.getGameMap();

        // 设置随机数种子
        GameCache.random.setSeed(gameMap.getSeed());

        // 初始化地图
        GameCache.map = new int[gameMap.getWidth() / Constants.PIXELS_PER_GRID][gameMap.getHeight() / Constants.PIXELS_PER_GRID];

        // 在地图上生成围墙
        gameMapService.generateMaze(GameCache.map, 0, 0, GameCache.map.length / 2, GameCache.map[0].length / 2);

        // 在地图上放置建筑
        gameMapService.placeAllBuildingsOnMap();

        // 放置没有主人的角色
        spriteService.getUnownedSprites().forEach(sprite ->
                GameCache.spriteCacheMap.put(sprite.getId(), new SpriteCache(
                        sprite.getX(),
                        sprite.getY(),
                        0,
                        0,
                        0,
                        System.currentTimeMillis(),
                        SpriteStatus.IDLE,
                        null,
                        null,
                        null,
                        null
                ))
        );
    }
}
