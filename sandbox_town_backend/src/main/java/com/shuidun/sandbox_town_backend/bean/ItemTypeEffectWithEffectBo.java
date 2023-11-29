package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeEffectWithEffectBo {
    @NonNull
    private ItemTypeEffectDo itemTypeEffect;

    @NonNull
    private EffectDo effect;
}
