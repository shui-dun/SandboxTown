package com.shuidun.sandbox_town_backend.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemBarNotifyVo {
    @Schema(description = "物品栏变化的精灵id")
    private String id;
}
