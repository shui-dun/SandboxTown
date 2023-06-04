package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    private String id;

    private String name;

    private String description;

    private Integer basicPrice;

    private Integer basicRarity;

    private Integer usable;

    private Integer moneyInc;

    private Integer expInc;

    private Integer levelInc;

    private Integer hungerInc;

    private Integer hpInc;

    private Integer attackInc;

    private Integer defenseInc;

    private Integer speedInc;
}
