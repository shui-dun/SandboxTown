package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeEffectBo extends ItemTypeEffectDo {
    private EffectDo effectObj;

    public ItemTypeEffectBo(ItemTypeEffectDo itemTypeEffectDo, EffectDo effectObj) {
        super(itemTypeEffectDo);
        this.effectObj = effectObj;
    }
}
