package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.SpriteType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SpriteMapper {
    // 根据角色id获取角色信息（包含sprite_type表中的角色描述信息）
    @Select("SELECT * FROM `sprite` INNER JOIN sprite_type " +
            "ON `sprite`.type = sprite_type.type " +
            "WHERE id = #{id}")
    Sprite getSpriteById(@Param("id") String id);


    @Update("UPDATE `sprite` SET ${attribute} = #{value} WHERE id = #{id}")
    void updateSpriteAttribute(@Param("id") String id, @Param("attribute") String attribute, @Param("value") int value);

    // 创建
    @Insert("INSERT INTO `sprite` (id, type, owner, money, exp, level, hunger, hp, attack, defense, speed, X, Y, width, height, map) " +
            "VALUES (#{id}, #{type}, #{owner}, #{money}, #{exp}, #{level}, #{hunger}, #{hp}, #{attack}, #{defense}, #{speed}, #{X}, #{Y}, #{width}, #{height}, #{map})")
    void createSprite(Sprite sprite);

    // 更新角色
    @Update("UPDATE `sprite` SET type = #{type}, owner = #{owner}, money = #{money}, exp = #{exp}, level = #{level}, hunger = #{hunger}, hp = #{hp}, attack = #{attack}, defense = #{defense}, speed = #{speed}, X = #{X}, Y = #{Y}, width = #{width}, height = #{height}, map = #{map} WHERE id = #{id}")
    void updateSprite(Sprite sprite);

    // 得到某个地图上的所有角色
    @Select("SELECT * FROM `sprite` WHERE map = #{map}")
    List<Sprite> getSpritesByMap(@Param("map") String map);

    // 得到角色类型信息
    @Select("SELECT * FROM sprite_type WHERE type = #{type}")
    SpriteType getSpriteType(@Param("type") String type);

}
