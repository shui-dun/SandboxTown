package com.shuidun.sandbox_town_backend.bean;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateVo {
    private String id;
    @JSONField(format = "0.00")
    private Double x;
    @JSONField(format = "0.00")
    private Double y;
    @JSONField(format = "0.00")
    private Double vx;
    @JSONField(format = "0.00")
    private Double vy;
}
