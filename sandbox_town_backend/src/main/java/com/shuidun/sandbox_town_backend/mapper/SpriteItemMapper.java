package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.SpriteItemView;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SpriteItemMapper {

    @Select("SELECT * FROM sprite_item_view WHERE owner = #{id}")
    public List<SpriteItemView> listByOwnerId(String id);

    @Select("SELECT * FROM sprite_item_view WHERE owner = #{ownerId} AND item_id = #{itemId}")
    SpriteItemView getByOwnerIdAndItemId(String ownerId, String itemId);

    @Delete("DELETE FROM sprite_item WHERE owner = #{ownerId} AND item_id = #{itemId}")
    void deleteByOwnerIdAndItemId(String ownerId, String itemId);

    @Update("UPDATE sprite_item SET item_count = #{i} WHERE owner = #{ownerId} AND item_id = #{itemId}")
    void updateByOwnerIdAndItemId(String ownerId, String itemId, int i);

    @Insert("INSERT INTO sprite_item (owner, item_id, item_count) VALUES (#{username}, #{itemId}, #{count})")
    void insert(String username, String itemId, int count);
}
