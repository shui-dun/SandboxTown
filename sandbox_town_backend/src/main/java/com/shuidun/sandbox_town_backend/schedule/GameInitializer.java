package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.GameMap;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import org.springframework.stereotype.Component;


/**
 * 游戏初始化器
 */
@Component
public class GameInitializer {

    public GameInitializer(GameMapService gameMapService) {

        // 获得地图信息
        GameMap gameMap = gameMapService.getGameMap();

        // 设置随机数种子
        GameCache.random.setSeed(gameMap.getSeed());

        // 初始化地图
        GameCache.map = new int[gameMap.getWidth() / Constants.PIXELS_PER_GRID][gameMap.getHeight() / Constants.PIXELS_PER_GRID];

        // 生成围墙
        gameMapService.generateMaze(GameCache.map, 0, 0, GameCache.map.length / 2, GameCache.map[0].length / 2);

        // 放置建筑
        gameMapService.placeAllBuildingsOnMap();
    }
}
