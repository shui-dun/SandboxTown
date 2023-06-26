package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type_attribute")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTypeAttribute {

    private String itemType;

    private String operation;

    private Integer moneyInc;

    private Integer expInc;

    private Integer levelInc;

    private Integer hungerInc;

    private Integer hpInc;

    private Integer attackInc;

    private Integer defenseInc;

    private Integer speedInc;
}