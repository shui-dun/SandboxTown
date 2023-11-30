package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import lombok.*;

import java.util.Map;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeDetailBo extends ItemTypeWithLabelsBo {
    private Map<ItemOperationEnum, ItemTypeAttributeDo> attributes;

    private Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectWithEffectBo>> effects;

    public ItemTypeDetailBo(ItemTypeWithLabelsBo itemTypeWithLabelsBo, Map<ItemOperationEnum, ItemTypeAttributeDo> attributes, Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectWithEffectBo>> effects) {
        super(itemTypeWithLabelsBo);
        this.attributes = attributes;
        this.effects = effects;
    }

}
