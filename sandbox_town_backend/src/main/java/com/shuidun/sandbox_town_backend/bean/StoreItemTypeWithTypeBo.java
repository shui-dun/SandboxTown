package com.shuidun.sandbox_town_backend.bean;

import lombok.*;
import org.springframework.lang.NonNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeWithTypeBo extends StoreItemTypeDo {
    @NonNull
    private ItemTypeDo itemTypeObj;
}