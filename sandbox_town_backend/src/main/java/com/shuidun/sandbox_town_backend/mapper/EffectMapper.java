package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.EffectDo;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface EffectMapper extends BaseMapper<EffectDo> {

}
