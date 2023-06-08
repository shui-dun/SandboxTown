package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.SpriteItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ItemMapper {

    @Select("SELECT sprite_item.owner, sprite_item.item_id, sprite_item.item_count, " +
            "item.name, item.description, item.basic_price, item.basic_rarity, item.usable, " +
            "item.money_inc, item.exp_inc, item.level_inc, item.hunger_inc, item.hp_inc, " +
            "item.attack_inc, item.defense_inc, item.speed_inc\n" +
            "FROM sprite_item\n" +
            "INNER JOIN item\n" +
            "ON sprite_item.item_id = item.id\n" +
            "WHERE sprite_item.owner = #{id}")
    public List<SpriteItem> listByOwnerId(String id);

    @Select("SELECT sprite_item.owner, sprite_item.item_id, sprite_item.item_count, " +
            "item.name, item.description, item.basic_price, item.basic_rarity, item.usable, " +
            "item.money_inc, item.exp_inc, item.level_inc, item.hunger_inc, item.hp_inc, " +
            "item.attack_inc, item.defense_inc, item.speed_inc\n" +
            "FROM sprite_item\n" +
            "INNER JOIN item\n" +
            "ON sprite_item.item_id = item.id\n" +
            "WHERE sprite_item.owner = #{ownerId} AND sprite_item.item_id = #{itemId}")
    SpriteItem getByOwnerIdAndItemId(String ownerId, String itemId);

    @Delete("DELETE FROM sprite_item WHERE owner = #{ownerId} AND item_id = #{itemId}")
    void deleteByOwnerIdAndItemId(String ownerId, String itemId);

    @Update("UPDATE sprite_item SET item_count = #{i} WHERE owner = #{ownerId} AND item_id = #{itemId}")
    void updateByOwnerIdAndItemId(String ownerId, String itemId, int i);

    @Insert("INSERT INTO sprite_item (owner, item_id, item_count) VALUES (#{username}, #{itemId}, #{count})")
    void insert(String username, String itemId, int count);
}
