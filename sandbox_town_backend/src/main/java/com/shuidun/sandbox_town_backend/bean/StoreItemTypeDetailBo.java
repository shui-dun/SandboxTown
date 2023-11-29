package com.shuidun.sandbox_town_backend.bean;

import lombok.*;
import org.springframework.lang.NonNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeDetailBo extends StoreItemTypeDo {
    @NonNull
    private ItemTypeDetailBo itemTypeObj;

    public StoreItemTypeDetailBo(StoreItemTypeDo storeItemTypeDo, ItemTypeDetailBo itemTypeObj) {
        super(storeItemTypeDo);
        this.itemTypeObj = itemTypeObj;
    }
}