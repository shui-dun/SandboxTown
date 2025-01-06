package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
