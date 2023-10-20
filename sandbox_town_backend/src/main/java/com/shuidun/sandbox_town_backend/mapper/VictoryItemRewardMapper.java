package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.VictoryItemRewardDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VictoryItemRewardMapper {
    @Select("select * from victory_item_reward where sprite_type = #{spriteType}")
    List<VictoryItemRewardDo> selectBySpriteType(SpriteTypeEnum spriteType);
}
