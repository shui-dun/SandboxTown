package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.SpriteRefreshDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SpriteRefreshMapper {

    @Select("select * from sprite_refresh where refresh_time = #{refreshTime}")
    List<SpriteRefreshDo> selectByTime(TimeFrameEnum refreshTime);

    @Select("select sprite_type from sprite_refresh where refresh_time = #{refreshTime}")
    List<SpriteTypeEnum> selectSpriteTypesByTime(TimeFrameEnum refreshTime);
}
