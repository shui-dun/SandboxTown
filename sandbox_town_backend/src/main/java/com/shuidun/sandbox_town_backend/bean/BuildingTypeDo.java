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
    @NonNull
    private BuildingTypeEnum id;

    @NonNull
    private String description;

    @NonNull
    private Integer basicPrice;

    @NonNull
    private String imagePath;

    @NonNull
    private Double basicWidth;

    @NonNull
    private Double basicHeight;

    @NonNull
    private Integer rarity;
}
