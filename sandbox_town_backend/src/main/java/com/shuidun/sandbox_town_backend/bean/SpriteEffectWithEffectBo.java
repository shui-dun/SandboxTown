package com.shuidun.sandbox_town_backend.bean;

import lombok.*;
import org.springframework.lang.NonNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteEffectWithEffectBo extends SpriteEffectDo {
    private EffectDo effectObj;

    public SpriteEffectWithEffectBo(SpriteEffectDo spriteEffectDo, EffectDo effectDo) {
        super(spriteEffectDo);
        this.effectObj = effectDo;
    }
}
