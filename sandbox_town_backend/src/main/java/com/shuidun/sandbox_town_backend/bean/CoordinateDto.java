package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinateDto {
    @NotNull
    private String id;
    @NotNull
    private Double x;
    @NotNull
    private Double y;
    @NotNull
    private Long time;
    // 这两个变量还没有被用到过
    @NotNull
    private Double vx;
    @NotNull
    private Double vy;
}
