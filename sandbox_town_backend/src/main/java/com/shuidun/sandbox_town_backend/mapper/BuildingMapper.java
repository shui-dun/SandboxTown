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
    @Select("SELECT * FROM building WHERE map = #{mapName}")
    List<Building> getAllBuildingsByMapName(String mapName);
}
