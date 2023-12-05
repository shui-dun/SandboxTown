package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.BuildingDo;
import com.shuidun.sandbox_town_backend.bean.BuildingTypeDo;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class BuildingService {

    @Lazy
    @Autowired
    private BuildingService self;

    private final BuildingMapper buildingMapper;

    private final BuildingTypeMapper buildingTypeMapper;

    @Value("${mapId}")
    private String mapId;

    public BuildingService(BuildingMapper buildingMapper, BuildingTypeMapper buildingTypeMapper) {
        this.buildingMapper = buildingMapper;
        this.buildingTypeMapper = buildingTypeMapper;
    }

    /** 获取所有建筑类型 */
    @Cacheable(value = "building::buildingTypes")
    public List<BuildingTypeDo> getAllBuildingTypes() {
        return buildingTypeMapper.selectList(null);
    }

    @Cacheable(value = "building::buildings", key = "#mapId")
    public List<BuildingDo> getAllBuildings(String mapId) {
        return buildingMapper.selectByMapId(mapId);
    }

    /** 查找某个地图上的所有建筑 */
    public List<BuildingDo> getAllBuildings() {
        return self.getAllBuildings(mapId);
    }
}
