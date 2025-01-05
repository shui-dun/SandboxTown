package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeBo extends StoreItemTypeDo {
    private ItemTypeBo itemTypeObj;

    public StoreItemTypeBo(StoreItemTypeDo storeItemTypeDo, ItemTypeBo itemTypeObj) {
        super(storeItemTypeDo);
        this.itemTypeObj = itemTypeObj;
    }
}