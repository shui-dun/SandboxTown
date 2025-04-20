package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EcosystemTypeEnum;

import lombok.Data;

@Data
public class EcosystemDo {
    private String id;
    private EcosystemTypeEnum type;
    private double centerX;
    private double centerY;
    private double width;
    private double height;
}