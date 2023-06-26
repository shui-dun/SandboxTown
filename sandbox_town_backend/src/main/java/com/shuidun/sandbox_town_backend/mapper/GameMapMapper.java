package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.shuidun.sandbox_town_backend.bean.GameMap;

@Mapper
public interface GameMapMapper extends BaseMapper<GameMap> {

}
