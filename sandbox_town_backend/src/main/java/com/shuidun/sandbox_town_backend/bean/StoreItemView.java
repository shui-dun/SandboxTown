package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemView {

    private String item;

    private String store;

    private Integer count;

    private Integer price;

    private String name;

    private String description;

    private Integer basicPrice;

    private Integer rarity;

    private Boolean usable;

    private Integer moneyInc;

    private Integer expInc;

    private Integer levelInc;

    private Integer hungerInc;

    private Integer hpInc;

    private Integer attackInc;

    private Integer defenseInc;

    private Integer speedInc;
}
