package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("building_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingTypeDo {

    @TableId
    private BuildingTypeEnum id;

    private String description;

    private Integer basicPrice;

    private String imagePath;

    private Double basicWidth;

    private Double basicHeight;

    private Integer rarity;
}
