package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@TableName("building")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDo {

    @TableId
    @NonNull
    private String id;

    @NonNull
    private BuildingTypeEnum type;

    @NonNull
    private String map;

    @NonNull
    private Integer level;

    @Nullable
    private SpriteTypeEnum owner;

    @NonNull
    private Double originX;

    @NonNull
    private Double originY;

    @NonNull
    private Double width;

    @NonNull
    private Double height;
}
