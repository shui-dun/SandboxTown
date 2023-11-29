package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.Map;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeDetailBo extends ItemTypeWithLabelsBo {
    @NonNull
    private Map<ItemOperationEnum, ItemTypeAttributeDo> attributes;

    @NonNull
    private Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectWithEffectBo>> effects;

}
