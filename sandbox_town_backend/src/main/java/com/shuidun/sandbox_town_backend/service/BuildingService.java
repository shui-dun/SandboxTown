package com.shuidun.sandbox_town_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.shuidun.sandbox_town_backend.bean.Building;
import com.shuidun.sandbox_town_backend.bean.BuildingType;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class BuildingService {
    private final BuildingMapper buildingMapper;

    private final BuildingTypeMapper buildingTypeMapper;

    @Value("${mapId}")
    private String mapId;

    public BuildingService(BuildingMapper buildingMapper, BuildingTypeMapper buildingTypeMapper) {
        this.buildingMapper = buildingMapper;
        this.buildingTypeMapper = buildingTypeMapper;
    }

    // 获取所有建筑类型
    public List<BuildingType> getAllBuildingTypes() {
        return buildingTypeMapper.selectList(null);
    }

    // 查找某个地图上的所有建筑
    public List<Building> getAllBuildings() {
        return buildingMapper.getAllBuildingsByMapId(mapId);
    }
}
