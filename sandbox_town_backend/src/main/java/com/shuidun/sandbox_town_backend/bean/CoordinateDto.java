package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateDto {
    private String id;
    private Double x;
    private Double y;
    // 这两个变量还没有被用到过
    private Double vx;
    private Double vy;
}
