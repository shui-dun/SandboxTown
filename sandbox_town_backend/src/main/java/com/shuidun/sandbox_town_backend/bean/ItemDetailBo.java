package com.shuidun.sandbox_town_backend.bean;

import lombok.*;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailBo extends ItemDo {
    private ItemTypeDetailBo itemTypeObj;

    public ItemDetailBo(ItemDo itemDo, ItemTypeDetailBo itemTypeDetailBo) {
        super(itemDo);
        this.itemTypeObj = itemTypeDetailBo;
    }
}
