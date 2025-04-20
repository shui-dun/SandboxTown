package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;

@Data
public class EcosystemTypeDo {
    private String id;
    private String name;
    private double basicWidth;
    private double basicHeight;
    private int rarity;
}