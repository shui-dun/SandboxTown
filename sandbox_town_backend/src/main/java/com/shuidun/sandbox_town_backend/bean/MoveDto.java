package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveDto {
    /** 出发点x坐标 */
    @NotNull
    private Double x0;
    /** 出发点y坐标 */
    @NotNull
    private Double y0;
    /** 终点x坐标 */
    @NotNull
    private Double x1;
    /** 终点y坐标 */
    @NotNull
    private Double y1;
    /** 终点建筑ID（如果有） */
    @Nullable
    private String destBuildingId;
    /** 终点精灵ID（如果有） */
    @Nullable
    private String destSpriteId;
}
