package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

import javax.validation.constraints.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithTypeAndLabelsBo extends ItemDo {
    @NotNull
    private ItemTypeWithLabelsBo itemTypeObj;

    public ItemWithTypeAndLabelsBo(ItemDo itemDo, ItemTypeWithLabelsBo itemTypeObj) {
        super(itemDo);
        this.itemTypeObj = itemTypeObj;
    }
}
