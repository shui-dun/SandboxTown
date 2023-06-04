package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.CharacterItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ItemMapper {

    @Select("SELECT character_item.owner, character_item.item_id, character_item.item_count, " +
            "item.name, item.description, item.basic_price, item.basic_rarity, item.usable, " +
            "item.money_inc, item.exp_inc, item.level_inc, item.hunger_inc, item.hp_inc, " +
            "item.attack_inc, item.defense_inc, item.speed_inc\n" +
            "FROM character_item\n" +
            "INNER JOIN item\n" +
            "ON character_item.item_id = item.id\n" +
            "WHERE character_item.owner = #{id}")
    public List<CharacterItem> listByOwnerId(String id);

    @Select("SELECT character_item.owner, character_item.item_id, character_item.item_count, " +
            "item.name, item.description, item.basic_price, item.basic_rarity, item.usable, " +
            "item.money_inc, item.exp_inc, item.level_inc, item.hunger_inc, item.hp_inc, " +
            "item.attack_inc, item.defense_inc, item.speed_inc\n" +
            "FROM character_item\n" +
            "INNER JOIN item\n" +
            "ON character_item.item_id = item.id\n" +
            "WHERE character_item.owner = #{ownerId} AND character_item.item_id = #{itemId}")
    CharacterItem getByOwnerIdAndItemId(String ownerId, String itemId);

    @Delete("DELETE FROM character_item WHERE owner = #{ownerId} AND item_id = #{itemId}")
    void deleteByOwnerIdAndItemId(String ownerId, String itemId);

    @Update("UPDATE character_item SET item_count = #{i} WHERE owner = #{ownerId} AND item_id = #{itemId}")
    void updateCountByOwnerIdAndItemId(String ownerId, String itemId, int i);
}
