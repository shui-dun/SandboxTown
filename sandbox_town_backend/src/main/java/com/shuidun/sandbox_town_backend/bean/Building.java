package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("building")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Building {

    @TableId
    private String id;

    private BuildingTypeEnum type;

    private String map;

    private Integer level;

    private SpriteTypeEnum owner;

    private Integer originX;

    private Integer originY;

    private Integer width;

    private Integer height;
}
