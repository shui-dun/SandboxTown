package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithTypeBo {
    @NotNull
    private ItemDo item;
    @NotNull
    private ItemTypeDo itemType;
}
