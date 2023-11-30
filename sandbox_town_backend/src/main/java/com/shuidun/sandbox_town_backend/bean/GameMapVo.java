package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.mixin.GameCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMapVo {

    private String id;

    private String name;

    private Integer width;

    private Integer height;

    /** 数据 */
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
