package com.shuidun.sandbox_town_backend.bean;

import lombok.*;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemBo extends ItemDo {
    private ItemTypeBo itemTypeObj;

    public ItemBo(ItemDo itemDo, ItemTypeBo itemTypeBo) {
        super(itemDo);
        this.itemTypeObj = itemTypeBo;
    }
}
