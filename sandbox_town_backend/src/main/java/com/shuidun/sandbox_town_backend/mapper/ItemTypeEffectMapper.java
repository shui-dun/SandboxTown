package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ItemTypeEffectDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemTypeEffectMapper extends BaseMapper<ItemTypeEffectDo> {
    /* 根据物品类型获得物品类型效果列表 */
    default List<ItemTypeEffectDo> selectByItemType(ItemTypeEnum itemType) {
        return selectList(new LambdaQueryWrapper<ItemTypeEffectDo>()
                .eq(ItemTypeEffectDo::getItemType, itemType));
    }
}
