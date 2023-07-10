package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("building_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildingTypeDo {

    @TableId
    private BuildingTypeEnum id;

    private String description;

    private Integer basicPrice;

    private String imagePath;

    private Integer basicWidth;

    private Integer basicHeight;

    private Integer rarity;
}
