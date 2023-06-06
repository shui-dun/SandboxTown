package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/gamemap")
public class GameMapController {

    private final GameMapService gameMapService;

    public GameMapController(GameMapService gameMapService) {
        this.gameMapService = gameMapService;
    }


    /** 得到地图的信息 */
    @GetMapping("/getGameMap")
    public RestResponse<?> getGameMap() {
        log.info("getGameMap: {}", new RestResponse<>(StatusCodeEnum.SUCCESS, gameMapService.getGameMap()));
        return new RestResponse<>(StatusCodeEnum.SUCCESS, gameMapService.getGameMap());
    }

    /** 初始化地图（建造随机的生态系统等） */
    @SaCheckRole("admin")
    @GetMapping("/initGameMap")
    public RestResponse<?> initGameMap() {
        gameMapService.initGameMap();
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }
}
