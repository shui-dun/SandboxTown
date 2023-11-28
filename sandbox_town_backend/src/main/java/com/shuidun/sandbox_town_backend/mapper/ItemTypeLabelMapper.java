package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ItemTypeLabelDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface ItemTypeLabelMapper extends BaseMapper<ItemTypeLabelDo> {
    /** 获得物品类型所对应的所有标签 */
    default Set<ItemLabelEnum> selectByItemType(ItemTypeEnum itemType) {
        return selectList(new LambdaQueryWrapper<ItemTypeLabelDo>()
                .eq(ItemTypeLabelDo::getItemType, itemType))
                .stream()
                .map(ItemTypeLabelDo::getLabel)
                .collect(Collectors.toSet());
    }

    default List<ItemTypeLabelDo> selectByItemTypes(List<ItemTypeEnum> itemTypes) {
        return selectList(new LambdaQueryWrapper<ItemTypeLabelDo>()
                .in(ItemTypeLabelDo::getItemType, itemTypes));
    }
}
