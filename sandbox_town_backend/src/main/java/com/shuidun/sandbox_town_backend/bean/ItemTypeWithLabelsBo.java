package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import lombok.*;

import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeWithLabelsBo extends ItemTypeDo {
    private Set<ItemLabelEnum> labels;

    public ItemTypeWithLabelsBo(ItemTypeDo itemTypeDo, Set<ItemLabelEnum> labels) {
        super(itemTypeDo);
        this.labels = labels;
    }

    public ItemTypeWithLabelsBo(ItemTypeWithLabelsBo other) {
        super(other);
        this.labels = other.labels;
    }
}
