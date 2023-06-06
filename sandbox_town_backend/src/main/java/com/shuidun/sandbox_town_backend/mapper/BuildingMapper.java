package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.Building;
import com.shuidun.sandbox_town_backend.bean.BuildingType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingMapper {

    /** 获取所有建筑类型 */
    @Select("SELECT * FROM building_type")
    List<BuildingType> getAllBuildingTypes();

    /** 根据地图名称获取上面所有的建筑物 */
    @Select("SELECT * FROM building WHERE map = #{mapId}")
    List<Building> getAllBuildingsByMapId(String mapId);

    // 当前建筑数目
    @Select("SELECT COUNT(*) FROM building WHERE map = #{mapId}")
    int getBuildingCountByMapId(String mapId);

    // 创建建筑
    @Select("INSERT INTO building (id, type, map, level, owner, origin_x, origin_y, width, height) VALUES (#{id}, #{type}, #{map}, #{level}, #{owner}, #{originX}, #{originY}, #{width}, #{height})")
    void createBuilding(Building building);
}
