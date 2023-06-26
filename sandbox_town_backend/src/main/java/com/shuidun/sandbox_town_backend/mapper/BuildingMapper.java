package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.Building;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingMapper extends BaseMapper<Building> {

    /** 根据地图名称获取上面所有的建筑物 */
    @Select("SELECT * FROM building WHERE map = #{mapId}")
    List<Building> selectByMapId(String mapId);

    // 地图上的建筑数目
    @Select("SELECT COUNT(*) FROM building WHERE map = #{mapId}")
    int countByMapId(String mapId);

    /** 根据地图名称和建筑物类型获取建筑物列表 */
    @Select("SELECT * FROM building WHERE map = #{mapId} AND type = #{type}")
    List<Building> selectByMapIdAndType(String mapId, BuildingTypeEnum type);
}
