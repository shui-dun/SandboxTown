package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type_effect")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeEffectDo {

    private ItemTypeEnum itemType;

    private ItemOperationEnum operation;

    private EffectEnum effect;

    private Integer duration;

    public ItemTypeEffectDo(ItemTypeEffectDo other) {
        this.itemType = other.itemType;
        this.operation = other.operation;
        this.effect = other.effect;
        this.duration = other.duration;
    }
}
