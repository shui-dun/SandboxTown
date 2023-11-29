package com.shuidun.sandbox_town_backend.bean;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateVo {
    @NonNull
    private String id;
    @JSONField(format = "0.00")
    @NonNull
    private Double x;
    @JSONField(format = "0.00")
    @NonNull
    private Double y;
    @JSONField(format = "0.00")
    @NonNull
    private Double vx;
    @JSONField(format = "0.00")
    @NonNull
    private Double vy;
}
