package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("item_type_attribute")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeAttributeDo {

    private ItemTypeEnum itemType;

    private ItemOperationEnum operation;

    private Integer moneyInc;

    private Integer expInc;

    private Integer levelInc;

    private Integer hungerInc;

    private Integer hpInc;

    private Integer attackInc;

    private Integer defenseInc;

    private Integer speedInc;

    private Integer visionRangeInc;

    private Integer attackRangeInc;
}
