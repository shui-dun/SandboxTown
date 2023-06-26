package com.shuidun.sandbox_town_backend.controller;

import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.BuildingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/building")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    // 得到所有建筑类型
    @GetMapping("/getAllBuildingTypes")
    public RestResponse<?> getAllBuildingTypes() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, buildingService.getAllBuildingTypes());
    }

    // 得到某个地图上的所有建筑
    @GetMapping("/getAllBuildings")
    public RestResponse<?> getAllBuildings() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, buildingService.getAllBuildings());
    }
}
