package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.FeedDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FeedMapper extends BaseMapper<FeedDo> {
    default List<FeedDo> selectBySpriteType(SpriteTypeEnum type) {
        return selectList(new LambdaQueryWrapper<FeedDo>()
                .eq(FeedDo::getSpriteType, type));
    }
}
