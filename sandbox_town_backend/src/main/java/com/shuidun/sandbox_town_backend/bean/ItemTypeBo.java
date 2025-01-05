package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import lombok.*;

import java.util.Map;
import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeBo extends ItemTypeDo {
    private Set<ItemLabelEnum> labels;

    private Map<ItemOperationEnum, ItemTypeAttributeDo> attributes;

    private Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectBo>> effects;

    public ItemTypeBo(ItemTypeDo itemTypeDo, Set<ItemLabelEnum> labels, Map<ItemOperationEnum, ItemTypeAttributeDo> attributes, Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectBo>> effects) {
        super(itemTypeDo);
        this.labels = labels;
        this.attributes = attributes;
        this.effects = effects;
    }

}
