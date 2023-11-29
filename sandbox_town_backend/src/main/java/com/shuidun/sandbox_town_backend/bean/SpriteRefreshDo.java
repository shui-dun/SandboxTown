package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("sprite_refresh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteRefreshDo {

    @NonNull
    private SpriteTypeEnum spriteType;

    @NonNull
    private BuildingTypeEnum buildingType;

    @NonNull
    private Integer minCount;

    @NonNull
    private Integer maxCount;

    @NonNull
    private TimeFrameEnum refreshTime;

}
