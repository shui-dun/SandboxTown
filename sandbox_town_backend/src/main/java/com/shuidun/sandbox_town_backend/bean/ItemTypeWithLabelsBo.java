package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeWithLabelsBo {
    @NonNull
    private ItemTypeDo itemType;

    @NonNull
    private Set<ItemLabelEnum> labels;
}
