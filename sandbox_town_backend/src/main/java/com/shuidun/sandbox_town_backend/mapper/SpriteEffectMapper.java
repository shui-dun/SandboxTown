package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import org.apache.ibatis.annotations.*;
import com.shuidun.sandbox_town_backend.bean.SpriteEffectDo;

import java.util.List;

@Mapper
public interface SpriteEffectMapper {
    // 根据精灵id查询精灵效果
    @Select("select * from sprite_effect where sprite = #{sprite}")
    List<SpriteEffectDo> selectBySprite(String sprite);

    // 根据精灵id和效果查询精灵效果
    @Select("select * from sprite_effect where sprite = #{sprite} and effect = #{effect}")
    SpriteEffectDo selectBySpriteAndEffect(String sprite, EffectEnum effect);

    @Update("update sprite_effect set expire = #{expire}, duration = #{duration} where sprite = #{sprite} and effect = #{effect}")
    void update(SpriteEffectDo spriteEffectDo);

    @Insert("insert into sprite_effect values(#{sprite}, #{effect}, #{duration}, #{expire})")
    void insert(SpriteEffectDo spriteEffectDo);

    @Delete("delete from sprite_effect where sprite = #{spriteId} and effect = #{effect}")
    void deleteBySpriteAndEffect(String spriteId, EffectEnum effect);

    /** 如果精灵已经存在该效果则更新，否则插入 */
    @Insert("insert into sprite_effect values(#{sprite}, #{effect}, #{duration}, #{expire}) on duplicate key update expire = #{expire}, duration = #{duration}")
    void insertOrUpdate(SpriteEffectDo spriteEffectDo);
}
