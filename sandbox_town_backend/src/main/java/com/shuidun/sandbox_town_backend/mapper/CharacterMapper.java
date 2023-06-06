package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.Character;
import com.shuidun.sandbox_town_backend.bean.CharacterType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CharacterMapper {
    // 根据角色id获取角色信息（包含character_type表中的角色描述信息）
    @Select("SELECT * FROM `character` INNER JOIN character_type " +
            "ON `character`.type = character_type.type " +
            "WHERE id = #{id}")
    Character getCharacterById(@Param("id") String id);


    @Update("UPDATE `character` SET ${attribute} = #{value} WHERE id = #{id}")
    void updateCharacterAttribute(@Param("id") String id, @Param("attribute") String attribute, @Param("value") int value);

    // 创建
    @Insert("INSERT INTO `character` (id, type, owner, money, exp, level, hunger, hp, attack, defense, speed, X, Y, width, height, map) " +
            "VALUES (#{id}, #{type}, #{owner}, #{money}, #{exp}, #{level}, #{hunger}, #{hp}, #{attack}, #{defense}, #{speed}, #{X}, #{Y}, #{width}, #{height}, #{map})")
    void createCharacter(Character character);

    // 更新角色
    @Update("UPDATE `character` SET type = #{type}, owner = #{owner}, money = #{money}, exp = #{exp}, level = #{level}, hunger = #{hunger}, hp = #{hp}, attack = #{attack}, defense = #{defense}, speed = #{speed}, X = #{X}, Y = #{Y}, width = #{width}, height = #{height}, map = #{map} WHERE id = #{id}")
    void updateCharacter(Character character);

    // 得到某个地图上的所有角色
    @Select("SELECT * FROM `character` WHERE map = #{map}")
    List<Character> getCharactersByMap(@Param("map") String map);

    // 得到角色类型信息
    @Select("SELECT * FROM character_type WHERE type = #{type}")
    CharacterType getCharacterType(@Param("type") String type);

}
