package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.GameMapDo;
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
        boolean containsBuilding = gameMapService.placeAllBuildingsOnMap();

        // 放置没有主人的角色
        spriteService.getUnownedSprites().forEach(sprite ->
                spriteService.online(sprite.getId())
        );

        // 如果没有建筑物，则生成一定数量的建筑物
        if (!containsBuilding) {
            gameMapService.createEnvironment(gameMap.getWidth() * gameMap.getHeight() / 300000);
        }
    }
}
