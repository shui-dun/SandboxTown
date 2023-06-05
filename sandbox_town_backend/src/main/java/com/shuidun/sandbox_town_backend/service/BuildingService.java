package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Building;
import com.shuidun.sandbox_town_backend.bean.BuildingType;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BuildingService {
    private final BuildingMapper buildingMapper;

    private final String mapId;

    public BuildingService(BuildingMapper buildingMapper, @Value("${mapId}") String mapId) {
        this.buildingMapper = buildingMapper;
        this.mapId = mapId;
    }

    // 获取所有建筑类型
    public List<BuildingType> getAllBuildingTypes() {
        return buildingMapper.getAllBuildingTypes();
    }

    // 查找某个地图上的所有建筑
    public List<Building> getAllBuildings() {
        return buildingMapper.getAllBuildingsByMapId(mapId);
    }
}
