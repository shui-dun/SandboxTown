package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeDetailBo extends StoreItemTypeDo {
    private ItemTypeDetailBo itemTypeObj;

    public StoreItemTypeDetailBo(StoreItemTypeDo storeItemTypeDo, ItemTypeDetailBo itemTypeObj) {
        super(storeItemTypeDo);
        this.itemTypeObj = itemTypeObj;
    }
}