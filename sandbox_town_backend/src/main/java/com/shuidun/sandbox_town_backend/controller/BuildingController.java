package com.shuidun.sandbox_town_backend.controller;

import com.shuidun.sandbox_town_backend.bean.BuildingDo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.BuildingService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/building")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @ApiOperation(value = "得到某个地图上的所有建筑")
    @GetMapping("/getAllBuildings")
    public RestResponseVo<List<BuildingDo>> getAllBuildings() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, buildingService.getAllBuildings());
    }
}
