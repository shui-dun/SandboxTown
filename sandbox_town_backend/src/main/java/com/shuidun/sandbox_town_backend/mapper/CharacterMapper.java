package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.Character;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PlayerMapper {
    @Select("SELECT * FROM player WHERE username = #{username}")
    Character getPlayerByUsername(@Param("username") String username);

    @Update("UPDATE player SET ${attribute} = #{value} WHERE username = #{username}")
    void updatePlayerAttribute(@Param("username") String username, @Param("attribute") String attribute, @Param("value") int value);

    @Insert("INSERT INTO player (username, money, exp, level, hunger, hp, attack, defense, speed) " +
            "VALUES (#{player.username}, #{player.money}, #{player.exp}, #{player.level}, #{player.hunger}, " +
            "#{player.hp}, #{player.attack}, #{player.defense}, #{player.speed})")
    void insertPlayer(@Param("player") Character character);

    @Update("UPDATE player SET money = #{money}, exp = #{exp}, level = #{level}, hunger = #{hunger}, hp = #{hp}, " +
            "attack = #{attack}, defense = #{defense}, speed = #{speed} WHERE username = #{username}")
    void updatePlayer(Character character);
}
