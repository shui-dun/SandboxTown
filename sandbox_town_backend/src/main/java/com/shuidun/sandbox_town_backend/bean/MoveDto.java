package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveDto {
    // 出发点x坐标
    private Integer x0;
    // 出发点y坐标
    private Integer y0;
    // 终点x坐标
    private Integer x1;
    // 终点y坐标
    private Integer y1;
    // 终点建筑ID（如果有）
    private String destBuildingId;
    // 终点精灵ID（如果有）
    private String destSpriteId;
}
