package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.PlayerItem;
import lombok.Data;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ItemMapper {

    @Select("SELECT player_item.owner, player_item.item_id, player_item.item_count, " +
            "item.name, item.description, item.basic_price, item.basic_rarity, item.usable, " +
            "item.money_inc, item.exp_inc, item.level_inc, item.hunger_inc, item.hp_inc, " +
            "item.attack_inc, item.defense_inc, item.speed_inc\n" +
            "FROM player_item\n" +
            "INNER JOIN item\n" +
            "ON player_item.item_id = item.id\n" +
            "WHERE player_item.owner = #{playerName}")
    public List<PlayerItem> listByUsername(String playerName);

    @Select("SELECT player_item.owner, player_item.item_id, player_item.item_count, " +
            "item.name, item.description, item.basic_price, item.basic_rarity, item.usable, " +
            "item.money_inc, item.exp_inc, item.level_inc, item.hunger_inc, item.hp_inc, " +
            "item.attack_inc, item.defense_inc, item.speed_inc\n" +
            "FROM player_item\n" +
            "INNER JOIN item\n" +
            "ON player_item.item_id = item.id\n" +
            "WHERE player_item.owner = #{username} AND player_item.item_id = #{itemId}")
    PlayerItem getByUsernameAndItemId(String username, String itemId);

    @Delete("DELETE FROM player_item WHERE owner = #{username} AND item_id = #{itemId}")
    void deleteByUsernameAndItemId(String username, String itemId);

    @Update("UPDATE player_item SET item_count = #{i} WHERE owner = #{username} AND item_id = #{itemId}")
    void updateCountByUsernameAndItemId(String username, String itemId, int i);
}
