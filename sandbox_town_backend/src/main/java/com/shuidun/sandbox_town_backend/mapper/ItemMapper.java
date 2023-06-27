package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.Item;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ItemMapper extends BaseMapper<Item> {

    // 根据角色id获得其物品信息
    @Select("SELECT * FROM item WHERE owner = #{owner}")
    List<Item> selectByOwner(String owner);

    // 根据角色id和位置获得其物品信息
    @Select("SELECT * FROM item WHERE owner = #{owner} AND position = #{position}")
    List<Item> selectByOwnerAndPosition(String owner, ItemPositionEnum position);

    // 根据角色id和物品类型获得其物品信息
    @Select("SELECT * FROM item WHERE owner = #{owner} AND item_type = #{itemType}")
    List<Item> selectByOwnerAndItemType(String owner, ItemTypeEnum itemType);
}
