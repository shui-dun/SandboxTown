package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type_effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTypeEffectDo {

    private ItemTypeEnum itemType;

    private ItemOperationEnum operation;

    private EffectEnum effect;

    private Integer duration;

    @TableField(exist = false)
    private EffectDo effectObj;
}
