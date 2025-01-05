package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ItemDo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<ItemDo> {

    /** 根据角色id获得其物品信息 */
    default List<ItemDo> selectByOwner(String owner) {
        return selectList(new LambdaQueryWrapper<ItemDo>()
                .eq(ItemDo::getOwner, owner));
    }
}
