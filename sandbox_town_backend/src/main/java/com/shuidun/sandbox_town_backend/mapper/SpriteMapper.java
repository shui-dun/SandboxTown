package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.SpriteType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SpriteMapper extends BaseMapper<Sprite> {
    // 根据角色id获取角色信息（包含sprite_type表中的角色描述信息）
    @Select("SELECT * FROM `sprite` INNER JOIN sprite_type " +
            "ON `sprite`.type = sprite_type.type " +
            "WHERE id = #{id}")
    Sprite getSpriteById(@Param("id") String id);


    @Update("UPDATE `sprite` SET ${attribute} = #{value} WHERE id = #{id}")
    void updateSpriteAttribute(@Param("id") String id, @Param("attribute") String attribute, @Param("value") int value);

    // 得到某个地图上的所有角色
    @Select("SELECT * FROM `sprite` WHERE map = #{map}")
    List<Sprite> getSpritesByMap(@Param("map") String map);

}
