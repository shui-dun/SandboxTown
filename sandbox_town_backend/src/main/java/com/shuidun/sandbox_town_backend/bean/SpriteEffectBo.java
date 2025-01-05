package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteEffectBo extends SpriteEffectDo {
    private EffectDo effectObj;

    public SpriteEffectBo(SpriteEffectDo spriteEffectDo, EffectDo effectDo) {
        super(spriteEffectDo);
        this.effectObj = effectDo;
    }
}
