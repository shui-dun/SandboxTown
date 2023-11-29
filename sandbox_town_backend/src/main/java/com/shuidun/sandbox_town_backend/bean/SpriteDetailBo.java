package com.shuidun.sandbox_town_backend.bean;

import lombok.*;
import org.springframework.lang.NonNull;

import java.util.List;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteDetailBo extends SpriteWithTypeBo {
    /**
     * 以下是查询精灵的装备和效果等信息后得到的字段
     * 玩家最后的属性值等于原先的属性值加上增量（装备或手持装备导致的属性变化）
     * 但注意: 没有moneyInc、expInc、levelInc
     */
    @NonNull
    private Integer hungerInc;

    @NonNull
    private Integer hpInc;

    @NonNull
    private Integer attackInc;

    @NonNull
    private Integer defenseInc;

    @NonNull
    private Integer speedInc;

    @NonNull
    private Integer visionRangeInc;

    @NonNull
    private Integer attackRangeInc;

    /** 效果列表 */
    @NonNull
    private List<SpriteEffectWithEffectBo> effects;

    /** 装备列表 */
    @NonNull
    private List<ItemDetailBo> equipments;

    public SpriteDetailBo(SpriteWithTypeBo spriteWithTypeBo) {
        super(spriteWithTypeBo);
    }
}
