package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.BuildingDo;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BuildingMapper extends BaseMapper<BuildingDo> {

    /** 根据地图名称获取上面所有的建筑物 */
    default List<BuildingDo> selectByMapId(String mapId) {
        return selectList(new LambdaQueryWrapper<BuildingDo>()
                .eq(BuildingDo::getMap, mapId));
    }

    /** 地图上的建筑数目 */
    default long countByMapId(String mapId) {
        return selectCount(new LambdaQueryWrapper<BuildingDo>()
                .eq(BuildingDo::getMap, mapId));
    }

    /** 根据地图名称和建筑物类型获取建筑物列表 */
    default List<BuildingDo> selectByMapIdAndType(String mapId, BuildingTypeEnum type) {
        return selectList(new LambdaQueryWrapper<BuildingDo>()
                .eq(BuildingDo::getMap, mapId)
                .eq(BuildingDo::getType, type));
    }

    /** 根据地图名称和建筑物类型列表获取建筑物列表 */
    default List<BuildingDo> selectByMapIdAndTypes(String mapId, List<BuildingTypeEnum> types) {
        return selectList(new LambdaQueryWrapper<BuildingDo>()
                .eq(BuildingDo::getMap, mapId)
                .in(BuildingDo::getType, types));
    }

    default void updateOwnerByOwner(String fromId, String toId) {
        update(null, new LambdaUpdateWrapper<BuildingDo>()
                .eq(BuildingDo::getOwner, fromId)
                .set(BuildingDo::getOwner, toId));
    }
}
