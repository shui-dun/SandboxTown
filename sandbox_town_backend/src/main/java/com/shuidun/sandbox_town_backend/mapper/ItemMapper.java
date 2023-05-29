package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.PlayerItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ItemMapper {

    @Select("SELECT player_item.owner, player_item.item_id, player_item.item_count, item.name, item.description, item.basicPrice, item.usable, item.expInc, item.hungerInc, item.hpInc, item.attackInc, item.defenseInc, item.speedInc\n" +
            "FROM player_item\n" +
            "INNER JOIN item\n" +
            "ON player_item.item_id = item.id\n" +
            "WHERE player_item.owner = #{playerName}")
    public List<PlayerItem> listByUsername(String playerName);
}
