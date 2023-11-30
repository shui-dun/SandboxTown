package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithTypeBo extends ItemDo {

    private ItemTypeDo itemTypeObj;

    public ItemWithTypeBo(ItemDo itemDo, ItemTypeDo itemTypeDo) {
        super(itemDo);
        this.itemTypeObj = itemTypeDo;
    }
}
