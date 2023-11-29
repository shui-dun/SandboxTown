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

    @NonNull
    private ItemTypeEnum itemType;

    @NonNull
    private ItemOperationEnum operation;

    @NonNull
    private Integer moneyInc;

    @NonNull
    private Integer expInc;

    @NonNull
    private Integer levelInc;

    @NonNull
    private Integer hungerInc;

    @NonNull
    private Integer hpInc;

    @NonNull
    private Integer attackInc;

    @NonNull
    private Integer defenseInc;

    @NonNull
    private Integer speedInc;

    @NonNull
    private Integer visionRangeInc;

    @NonNull
    private Integer attackRangeInc;
}
