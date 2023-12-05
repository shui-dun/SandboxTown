package com.shuidun.sandbox_town_backend.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteDetailBo extends SpriteWithTypeBo {
    @Schema(description = """
            xxInc是查询精灵的装备和效果等信息后得到的字段
            玩家最后的属性值等于原先的属性值加上增量（装备或手持装备导致的属性变化）
            但注意: 没有moneyInc、expInc、levelInc
            """)
    private Integer hungerInc;

    private Integer hpInc;

    private Integer attackInc;

    private Integer defenseInc;

    private Integer speedInc;

    private Integer visionRangeInc;

    private Integer attackRangeInc;

    @Schema(description = "效果列表")
    private List<SpriteEffectWithEffectBo> effects;

    @Schema(description = "装备列表")
    private List<ItemDetailBo> equipments;

    public SpriteDetailBo(SpriteWithTypeBo spriteWithTypeBo) {
        super(spriteWithTypeBo);
    }
}
