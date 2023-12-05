package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.mixin.GameCache;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMapVo {

    private String id;

    private String name;

    private Integer width;

    private Integer height;

    @Schema(description = "地图数据")
    private int[][] data;

    public static GameMapVo fromGameMapDo(GameMapDo gameMapDo) {
        GameMapVo gameMapVo = new GameMapVo();
        gameMapVo.setId(gameMapDo.getId());
        gameMapVo.setName(gameMapDo.getName());
        gameMapVo.setWidth(gameMapDo.getWidth());
        gameMapVo.setHeight(gameMapDo.getHeight());
        gameMapVo.setData(GameCache.map);
        return gameMapVo;
    }
}
