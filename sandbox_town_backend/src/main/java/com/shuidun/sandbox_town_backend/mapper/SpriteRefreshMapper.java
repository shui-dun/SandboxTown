package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteRefreshDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SpriteRefreshMapper extends BaseMapper<SpriteRefreshDo> {

    default List<SpriteRefreshDo> selectByTime(TimeFrameEnum refreshTime) {
        return selectList(new LambdaQueryWrapper<SpriteRefreshDo>()
                .eq(SpriteRefreshDo::getRefreshTime, refreshTime));
    }

    default List<SpriteTypeEnum> selectSpriteTypesByTime(TimeFrameEnum refreshTime) {
        return selectList(new LambdaQueryWrapper<SpriteRefreshDo>()
                .eq(SpriteRefreshDo::getRefreshTime, refreshTime))
                .stream()
                .map(SpriteRefreshDo::getSpriteType)
                .collect(Collectors.toList());
    }
}
