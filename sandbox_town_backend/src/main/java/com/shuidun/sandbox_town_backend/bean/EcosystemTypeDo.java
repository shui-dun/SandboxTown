package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EcosystemTypeEnum;

import lombok.Data;

@Data
public class EcosystemTypeDo {
    private EcosystemTypeEnum id;
    private String name;
    private double basicWidth;
    private double basicHeight;
    private int rarity;
}