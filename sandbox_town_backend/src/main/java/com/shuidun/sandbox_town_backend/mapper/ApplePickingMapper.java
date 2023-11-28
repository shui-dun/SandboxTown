package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ApplePickingDo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

@Mapper
public interface ApplePickingMapper extends BaseMapper<ApplePickingDo> {
    @Nullable
    default ApplePickingDo selectById(String spriteId, String treeId) {
        return selectOne(new LambdaQueryWrapper<ApplePickingDo>()
                .eq(ApplePickingDo::getSprite, spriteId)
                .eq(ApplePickingDo::getTree, treeId));
    }

    default void update(ApplePickingDo applePicking) {
        update(applePicking, new LambdaQueryWrapper<ApplePickingDo>()
                .eq(ApplePickingDo::getSprite, applePicking.getSprite())
                .eq(ApplePickingDo::getTree, applePicking.getTree()));
    }
}