package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.ItemTypeAttribute;
import org.apache.ibatis.annotations.Select;

public interface ItemTypeAttributeMapper {
    // 根据物品类型和操作获得物品类型属性
    @Select("select * from item_type_attribute where item_type=#{itemType} and operation=#{operation}")
    ItemTypeAttribute selectByItemTypeAndOperation(String itemType, String operation);
}
