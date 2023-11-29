package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeWithTypeBo {
    @NonNull
    private StoreItemTypeDo storeItemType;

    @NonNull
    private ItemTypeDo itemType;
}
