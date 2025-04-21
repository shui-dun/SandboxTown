package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.BuildingTypeDo;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class BuildingTypeService {
    
    private final BuildingTypeMapper buildingTypeMapper;

    @Lazy
    @Autowired
    private BuildingTypeService self;

    public BuildingTypeService(BuildingTypeMapper buildingTypeMapper) {
        this.buildingTypeMapper = buildingTypeMapper;
    }

    @Cacheable(value = "buildingType::all")
    public Map<BuildingTypeEnum, BuildingTypeDo> selectAll() {
        List<BuildingTypeDo> types = buildingTypeMapper.selectList(null);
        return types.stream()
                .collect(Collectors.toMap(BuildingTypeDo::getId, type -> type));
    }

    public BuildingTypeDo selectById(BuildingTypeEnum id) {
        return self.selectAll().get(id);
    }
}