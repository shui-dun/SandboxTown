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
    @NonNull
    private String id;
    @NotNull
    @NonNull
    private Double x;
    @NotNull
    @NonNull
    private Double y;
    @NotNull
    @NonNull
    private Long time;
    // 这两个变量还没有被用到过
    @NotNull
    @NonNull
    private Double vx;
    @NotNull
    @NonNull
    private Double vy;
}
