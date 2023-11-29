package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeWithLabelsBo extends ItemTypeDo {
    @NonNull
    private Set<ItemLabelEnum> labels;
}
