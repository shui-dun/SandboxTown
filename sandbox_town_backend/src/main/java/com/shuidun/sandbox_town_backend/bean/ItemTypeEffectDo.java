package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("item_type_effect")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeEffectDo {

    @NonNull
    private ItemTypeEnum itemType;

    @NonNull
    private ItemOperationEnum operation;

    @NonNull
    private EffectEnum effect;

    @NonNull
    private Integer duration;

    public ItemTypeEffectDo(ItemTypeEffectDo other) {
        this.itemType = other.itemType;
        this.operation = other.operation;
        this.effect = other.effect;
        this.duration = other.duration;
    }
}
