package com.shuidun.sandbox_town_backend.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 记录精灵的效果变化，只有玩家自己发生变化，
 * 或者虽然是其他精灵的变化，但会引起前端动画变化才会收到通知
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteEffectChangeVo {
    @ApiModelProperty(value = "精灵id")
    private String id;
}
