package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;

@Data
public class EcosystemDo {
    private String id;
    private String type;
    private double centerX;
    private double centerY;
    private double width;
    private double height;
}