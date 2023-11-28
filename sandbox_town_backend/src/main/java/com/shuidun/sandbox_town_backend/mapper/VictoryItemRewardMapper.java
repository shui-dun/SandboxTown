package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.VictoryItemRewardDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VictoryItemRewardMapper extends BaseMapper<VictoryItemRewardDo> {
    default List<VictoryItemRewardDo> selectBySpriteType(SpriteTypeEnum spriteType) {
        return selectList(new LambdaQueryWrapper<VictoryItemRewardDo>()
                .eq(VictoryItemRewardDo::getSpriteType, spriteType));
    }
}
