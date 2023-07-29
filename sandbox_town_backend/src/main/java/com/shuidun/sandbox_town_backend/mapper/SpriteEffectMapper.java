package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.shuidun.sandbox_town_backend.bean.SpriteEffectDo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SpriteEffectMapper {
    // 根据精灵id查询精灵效果
    @Select("select * from sprite_effect where sprite = #{sprite}")
    List<SpriteEffectDo> selectBySprite(String sprite);
}
