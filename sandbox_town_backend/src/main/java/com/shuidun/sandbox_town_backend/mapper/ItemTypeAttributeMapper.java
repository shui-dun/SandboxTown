package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.ItemTypeAttributeDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface ItemTypeAttributeMapper {
    /** 根据物品类型和操作获得物品类型属性 */
    @Select("select * from item_type_attribute where item_type=#{itemType} and operation=#{operation}")
    ItemTypeAttributeDo selectByItemTypeAndOperation(ItemTypeEnum itemType, ItemOperationEnum operation);

    /* 根据物品类型获得物品类型属性列表 */
    @Select("select * from item_type_attribute where item_type=#{itemType}")
    Set<ItemTypeAttributeDo> selectByItemType(ItemTypeEnum itemType);
}
