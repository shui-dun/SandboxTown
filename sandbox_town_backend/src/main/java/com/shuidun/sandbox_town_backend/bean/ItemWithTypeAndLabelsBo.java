package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithTypeAndLabelsBo extends ItemDo {
    private ItemTypeWithLabelsBo itemTypeObj;

    public ItemWithTypeAndLabelsBo(ItemDo itemDo, ItemTypeWithLabelsBo itemTypeObj) {
        super(itemDo);
        this.itemTypeObj = itemTypeObj;
    }
}
