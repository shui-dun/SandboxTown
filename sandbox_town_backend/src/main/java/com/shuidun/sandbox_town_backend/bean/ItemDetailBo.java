package com.shuidun.sandbox_town_backend.bean;

import lombok.*;

import javax.validation.constraints.NotNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDetailBo extends ItemDo {
    @NotNull
    private ItemTypeDetailBo itemTypeDetailObj;
}
