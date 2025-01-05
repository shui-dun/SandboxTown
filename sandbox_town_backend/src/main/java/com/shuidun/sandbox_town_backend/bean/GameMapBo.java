package com.shuidun.sandbox_town_backend.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMapBo {

    private String id;

    private String name;

    private Integer width;

    private Integer height;

    @Schema(description = "地图数据")
    private int[][] data;
}
