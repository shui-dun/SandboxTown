package com.shuidun.sandbox_town_backend.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InteractDto {
    @Schema(description = "交互事件的发起者")
    @NotNull
    private String source;
    @Schema(description = "交互事件的目标")
    @NotNull
    private String target;
    @Schema(description = "交互事件的序列号（用于去重）")
    @NotNull
    private Integer sn;
}
