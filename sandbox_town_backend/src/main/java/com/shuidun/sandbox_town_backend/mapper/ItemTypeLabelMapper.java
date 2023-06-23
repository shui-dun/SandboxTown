package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ItemTypeLabelMapper {
    // 获得物品类型所对应的所有标签
    @Select("select label from item_type_label where item_type=#{itemType}")
    List<String> selectByItemType(String itemType);
}
