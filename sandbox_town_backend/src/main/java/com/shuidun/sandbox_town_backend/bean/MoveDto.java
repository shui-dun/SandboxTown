package com.shuidun.sandbox_town_backend.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveDto {
    @ApiModelProperty(value = "出发点x坐标")
    @NotNull
    private Double x0;
    @ApiModelProperty(value = "出发点y坐标")
    @NotNull
    private Double y0;
    @ApiModelProperty(value = "终点x坐标")
    @NotNull
    private Double x1;
    @ApiModelProperty(value = "终点y坐标")
    @NotNull
    private Double y1;
    @ApiModelProperty(value = "终点建筑ID（如果有）")
    @Nullable
    private String destBuildingId;
    @ApiModelProperty(value = "终点精灵ID（如果有）")
    @Nullable
    private String destSpriteId;
}
