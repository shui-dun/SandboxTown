package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.FeedDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FeedMapper {


    @Select("select * from feed where sprite_type = #{type}")
    List<FeedDo> selectBySpriteType(SpriteTypeEnum type);
}
