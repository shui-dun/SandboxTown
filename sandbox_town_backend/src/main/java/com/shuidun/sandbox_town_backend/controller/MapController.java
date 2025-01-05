package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.shuidun.sandbox_town_backend.bean.GameMapBo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.MapService;
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
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }


    @Operation(summary = "得到地图的信息")
    @GetMapping("/getGameMap")
    public RestResponseVo<GameMapBo> getGameMap() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, mapService.getGameMapWithMap());
    }

    @Operation(summary = "建造随机的生态系统")
    @SaCheckRole("ADMIN")
    @GetMapping("/createEnvironment")
    public RestResponseVo<Void> createEnvironment(@NotNull @RequestParam int nBuildings) {
        mapService.createEnvironment(nBuildings);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
