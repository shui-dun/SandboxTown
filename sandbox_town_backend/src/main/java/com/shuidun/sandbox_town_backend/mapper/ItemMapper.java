package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ItemDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<ItemDo> {

    /** 根据角色id获得其物品信息 */
    default List<ItemDo> selectByOwner(String owner) {
        return selectList(new LambdaQueryWrapper<ItemDo>()
                .eq(ItemDo::getOwner, owner));
    }

    /** 根据角色id和位置获得其物品信息 */
    default List<ItemDo> selectByOwnerAndPosition(String owner, ItemPositionEnum position) {
        return selectList(new LambdaQueryWrapper<ItemDo>()
                .eq(ItemDo::getOwner, owner)
                .eq(ItemDo::getPosition, position));
    }

    /** 根据角色id和物品类型获得其物品信息 */
    default List<ItemDo> selectByOwnerAndItemType(String owner, ItemTypeEnum itemType) {
        return selectList(new LambdaQueryWrapper<ItemDo>()
                .eq(ItemDo::getOwner, owner)
                .eq(ItemDo::getItemType, itemType));
    }

    /**
     * 根据角色id和位置列表获得其物品信息
     * 即，物品的位置在列表中的任意一个即可
     */
    default List<ItemDo> selectByOwnerAndPositions(String owner, List<ItemPositionEnum> positions) {
        return selectList(new LambdaQueryWrapper<ItemDo>()
                .eq(ItemDo::getOwner, owner)
                .in(ItemDo::getPosition, positions));
    }
}
