package com.shuidun.sandbox_town_backend.mapper;
import com.shuidun.sandbox_town_backend.bean.ItemTypeEffectDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

public interface ItemTypeEffectMapper {
    /** 根据物品类型和操作获得物品类型效果列表 */
    @Select("select * from item_type_effect where item_type=#{itemType} and operation=#{operation}")
    Set<ItemTypeEffectDo> selectByItemTypeAndOperation(ItemTypeEnum itemType, ItemOperationEnum operation);

    /* 根据物品类型获得物品类型效果列表 */
    @Select("select * from item_type_effect where item_type=#{itemType}")
    Set<ItemTypeEffectDo> selectByItemType(ItemTypeEnum itemType);
}
