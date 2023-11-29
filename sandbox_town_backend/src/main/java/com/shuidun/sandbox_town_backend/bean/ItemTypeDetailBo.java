package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeDetailBo {

    @NonNull
    private ItemTypeDo itemType;

    @NonNull
    private Set<ItemLabelEnum> labels;

    @NonNull
    private Map<ItemOperationEnum, ItemTypeAttributeDo> attributes;

    @NonNull
    private Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectWithEffectBo>> effects;

}
