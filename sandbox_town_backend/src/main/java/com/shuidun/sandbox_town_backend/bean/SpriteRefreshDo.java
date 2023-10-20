package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite_refresh")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteRefreshDo {

    private SpriteTypeEnum spriteType;

    private BuildingTypeEnum buildingType;

    private Integer minCount;

    private Integer maxCount;

    private TimeFrameEnum refreshTime;

}
