package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.Player;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PlayerMapper {
    @Select("SELECT * FROM player WHERE username = #{username}")
    Player getPlayerByUsername(@Param("username") String username);

    @Update("UPDATE player SET ${attribute} = #{value} WHERE username = #{username}")
    void updatePlayerAttribute(@Param("username") String username, @Param("attribute") String attribute, @Param("value") int value);

    @Insert("INSERT INTO player (username, money, exp, level, hunger, hp, attack, defense, speed) " +
            "VALUES (#{player.username}, #{player.money}, #{player.exp}, #{player.level}, #{player.hunger}, " +
            "#{player.hp}, #{player.attack}, #{player.defense}, #{player.speed})")
    void insertPlayer(@Param("player") Player player);
}
