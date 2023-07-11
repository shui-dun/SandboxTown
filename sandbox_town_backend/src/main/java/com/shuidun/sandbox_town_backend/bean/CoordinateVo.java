package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateVo {
    private String id;
    private int x;
    private int y;
    private double vx;
    private double vy;
}
