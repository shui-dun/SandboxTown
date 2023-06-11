package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @TableId
    private String id;

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
