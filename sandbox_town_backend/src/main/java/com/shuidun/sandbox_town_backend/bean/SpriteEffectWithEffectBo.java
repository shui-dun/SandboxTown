package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteEffectWithEffectBo {
    @NonNull
    private SpriteEffectDo spriteEffect;

    @NonNull
    private EffectDo effect;
}
