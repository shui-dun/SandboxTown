package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ItemTypeAttributeDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

import java.util.List;

@Mapper
public interface ItemTypeAttributeMapper extends BaseMapper<ItemTypeAttributeDo> {
    /** 根据物品类型和操作获得物品类型属性 */
    @Nullable
    default ItemTypeAttributeDo selectById(ItemTypeEnum itemType, ItemOperationEnum operation) {
        return selectOne(new LambdaQueryWrapper<ItemTypeAttributeDo>()
                .eq(ItemTypeAttributeDo::getItemType, itemType)
                .eq(ItemTypeAttributeDo::getOperation, operation));
    }

    /* 根据物品类型获得物品类型属性列表 */
    default List<ItemTypeAttributeDo> selectByItemType(ItemTypeEnum itemType) {
        return selectList(new LambdaQueryWrapper<ItemTypeAttributeDo>()
                .eq(ItemTypeAttributeDo::getItemType, itemType));
    }
}
