package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.shuidun.sandbox_town_backend.bean.GameMapVo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.EcosystemService;
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

    private final EcosystemService ecosystemService;

    public GameMapController(GameMapService gameMapService, EcosystemService ecosystemService) {
        this.gameMapService = gameMapService;
        this.ecosystemService = ecosystemService;
    }


    @Operation(summary = "得到地图的信息")
    @GetMapping("/getGameMap")
    public RestResponseVo<GameMapVo> getGameMap() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                GameMapVo.fromGameMapDo(gameMapService.getGameMap()));
    }

    @Operation(summary = "建造随机的生态系统")
    @SaCheckRole("ADMIN")
    @GetMapping("/createEnvironment")
    public RestResponseVo<Void> createEnvironment(@NotNull @RequestParam int nBuildings) {
        ecosystemService.createEnvironment(nBuildings);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
