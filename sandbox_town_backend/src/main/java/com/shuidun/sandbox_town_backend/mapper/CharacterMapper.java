package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.Character;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CharacterMapper {
    @Select("SELECT * FROM `character` WHERE id = #{id}")
    Character getCharacterByID(@Param("id") String id);

    @Update("UPDATE `character` SET ${attribute} = #{value} WHERE id = #{id}")
    void updateCharacterAttribute(@Param("id") String id, @Param("attribute") String attribute, @Param("value") int value);

    @Insert("INSERT INTO `character` (id, owner, money, exp, level, hunger, hp, attack, defense, speed) " +
            "VALUES (#{character.id}, #{character.owner}, #{character.money}, " +
            "#{character.exp}, #{character.level}, #{character.hunger}, " +
            "#{character.hp}, #{character.attack}, #{character.defense}, #{character.speed})")
    void insertCharacter(@Param("character") Character character);

    // 更新角色
    @Update("UPDATE `character` SET owner = #{character.owner}, money = #{character.money}, " +
            "exp = #{character.exp}, level = #{character.level}, hunger = #{character.hunger}, " +
            "hp = #{character.hp}, attack = #{character.attack}, defense = #{character.defense}, " +
            "speed = #{character.speed} WHERE id = #{character.id}")
    void updateCharacter(@Param("character") Character character);
}
