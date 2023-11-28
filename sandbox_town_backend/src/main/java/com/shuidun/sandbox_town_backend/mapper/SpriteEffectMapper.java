package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteEffectDo;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.List;

@Mapper
public interface SpriteEffectMapper extends BaseMapper<SpriteEffectDo> {
    /** 根据精灵id查询精灵效果 */
    default List<SpriteEffectDo> selectBySprite(String sprite) {
        return selectList(new LambdaQueryWrapper<SpriteEffectDo>()
                .eq(SpriteEffectDo::getSprite, sprite));
    }

    /** 根据精灵id和效果查询精灵效果 */
    @Nullable
    default SpriteEffectDo selectBySpriteAndEffect(String sprite, EffectEnum effect) {
        return selectOne(new LambdaQueryWrapper<SpriteEffectDo>()
                .eq(SpriteEffectDo::getSprite, sprite)
                .eq(SpriteEffectDo::getEffect, effect));
    }

    default void update(SpriteEffectDo spriteEffectDo) {
        update(spriteEffectDo, new LambdaQueryWrapper<SpriteEffectDo>()
                .eq(SpriteEffectDo::getSprite, spriteEffectDo.getSprite())
                .eq(SpriteEffectDo::getEffect, spriteEffectDo.getEffect()));
    }

    default void deleteBySpriteAndEffect(String spriteId, EffectEnum effect) {
        delete(new LambdaQueryWrapper<SpriteEffectDo>()
                .eq(SpriteEffectDo::getSprite, spriteId)
                .eq(SpriteEffectDo::getEffect, effect));
    }

    /** 如果精灵已经存在该效果则更新，否则插入 */
    @Insert("insert into sprite_effect values(#{sprite}, #{effect}, #{duration}, #{expire}) on duplicate key update expire = #{expire}, duration = #{duration}")
    void insertOrUpdate(SpriteEffectDo spriteEffectDo);
}
