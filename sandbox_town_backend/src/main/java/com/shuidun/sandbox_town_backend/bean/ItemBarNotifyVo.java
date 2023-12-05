package com.shuidun.sandbox_town_backend.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemBarNotifyVo {
    @ApiModelProperty(value = "物品栏变化的精灵id")
    private String id;
}
