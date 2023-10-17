package com.shuidun.sandbox_town_backend.bean;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateVo {
    private String id;
    @JSONField(format = "0.00")
    private double x;
    @JSONField(format = "0.00")
    private double y;
    @JSONField(format = "0.00")
    private double vx;
    @JSONField(format = "0.00")
    private double vy;
}
