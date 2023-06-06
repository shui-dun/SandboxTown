package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildingType {

    private String id;

    private String description;

    private Integer basicPrice;

    private String imagePath;

    private Integer basicWidth;

    private Integer basicHeight;

    private Integer rarity;
}
