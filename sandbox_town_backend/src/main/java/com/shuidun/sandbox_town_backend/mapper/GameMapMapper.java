package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.shuidun.sandbox_town_backend.bean.GameMap;

@Mapper
public interface GameMapMapper {
    // 根据ID获取地图
    @Select("SELECT * FROM game_map WHERE id = #{id}")
    GameMap getGameMapById(String id);

}
