package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.FusionMaterialDo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FusionMaterialMapper extends BaseMapper<FusionMaterialDo> {
    default List<FusionMaterialDo> selectByFusionId(Integer fusionId) {
        return selectList(new LambdaQueryWrapper<FusionMaterialDo>()
            .eq(FusionMaterialDo::getFusionId, fusionId));
    }
}