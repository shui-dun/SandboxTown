package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SpriteMapper extends BaseMapper<SpriteDo> {

    /** 得到NPC */
    default List<SpriteDo> selectNPCs() {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .ne(SpriteDo::getType, SpriteTypeEnum.USER));
    }

    @Insert("""
            INSERT INTO sprite
            VALUES (#{id}, #{type}, #{owner}, #{money}, #{exp}, #{level}, #{hunger}, #{hp}, #{attack}, #{defense}, #{speed}, #{visionRange}, #{attackRange}, #{X}, #{Y}, #{width}, #{height}, #{map})
            ON DUPLICATE KEY UPDATE
            type = #{type}, owner = #{owner}, money = #{money}, exp = #{exp}, level = #{level}, hunger = #{hunger}, hp = #{hp}, attack = #{attack}, defense = #{defense}, speed = #{speed}, vision_range = #{visionRange}, attack_range = #{attackRange}, x = #{X}, y = #{Y}, width = #{width}, height = #{height}, map = #{map}
            """)
    void insertOrUpdateById(SpriteDo sprite);
}
