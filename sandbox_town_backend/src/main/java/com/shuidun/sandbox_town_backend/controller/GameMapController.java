package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.shuidun.sandbox_town_backend.bean.GameMapWithMap;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    @Operation(summary = "得到地图的信息")
    @GetMapping("/getGameMap")
    public RestResponseVo<GameMapWithMap> getGameMap() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, gameMapService.getGameMapWithMap());
    }

    @Operation(summary = "建造随机的生态系统")
    @SaCheckRole("ADMIN")
    @GetMapping("/createEnvironment")
    public RestResponseVo<Void> createEnvironment(@NotNull @RequestParam int nBuildings) {
        gameMapService.createEnvironment(nBuildings);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
