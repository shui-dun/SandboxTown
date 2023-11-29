package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

import javax.validation.constraints.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithTypeBo extends ItemDo {

    @NotNull
    private ItemTypeDo itemTypeObj;

    public ItemWithTypeBo(ItemDo itemDo, ItemTypeDo itemTypeDo) {
        super(itemDo);
        this.itemTypeObj = itemTypeDo;
    }
}
