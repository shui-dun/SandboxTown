package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@TableName("building")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDo {

    @TableId
    private String id;

    private BuildingTypeEnum type;

    private String map;

    private Integer level;

    @Nullable
    private String owner;

    private Double originX;

    private Double originY;

    private Double width;

    private Double height;
}
