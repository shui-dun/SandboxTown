package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.shuidun.sandbox_town_backend.bean.GameMapVo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Validated
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
    public RestResponseVo<?> getGameMap() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                GameMapVo.fromGameMapDo(gameMapService.getGameMap()));
    }

    /** 建造随机的生态系统 */
    @SaCheckRole("ADMIN")
    @GetMapping("/createEnvironment")
    public RestResponseVo<?> createEnvironment(@NotNull int nBuildings) {
        gameMapService.createEnvironment(nBuildings);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
