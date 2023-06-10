package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("building_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildingType {

    @TableId
    private String id;

    private String description;

    private Integer basicPrice;

    private String imagePath;

    private Integer basicWidth;

    private Integer basicHeight;

    private Integer rarity;
}
