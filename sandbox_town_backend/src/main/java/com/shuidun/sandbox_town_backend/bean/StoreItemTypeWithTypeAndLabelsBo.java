package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeWithTypeAndLabelsBo extends StoreItemTypeDo {
    private ItemTypeWithLabelsBo itemTypeObj;

    public StoreItemTypeWithTypeAndLabelsBo(StoreItemTypeDo storeItemTypeDo, ItemTypeWithLabelsBo itemTypeObj) {
        super(storeItemTypeDo);
        this.itemTypeObj = itemTypeObj;
    }
}