package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SpriteMapper extends BaseMapper<SpriteDo> {

    /** 得到NPC */
    default List<SpriteDo> selectNPCs() {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .ne(SpriteDo::getType, SpriteTypeEnum.USER));
    }

    /** 根据精灵类型和地图id得到精灵数量 */
    default long countByTypeAndMap(SpriteTypeEnum type, String map) {
        return selectCount(new LambdaQueryWrapper<SpriteDo>()
                .eq(SpriteDo::getType, type)
                .eq(SpriteDo::getMap, map));
    }

    /** 根据精灵类型列表和地图id得到精灵 */
    default List<SpriteDo> selectByTypesAndMap(List<SpriteTypeEnum> types, String map) {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .in(SpriteDo::getType, types)
                .eq(SpriteDo::getMap, map));
    }

}
