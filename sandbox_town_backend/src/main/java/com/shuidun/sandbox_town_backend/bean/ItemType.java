package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@TableName("item_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemType {

    @TableId
    private ItemTypeEnum id;

    private String name;

    private String description;

    private Integer basicPrice;

    private Integer rarity;

    private Integer durability;

    @TableField(exist = false)
    private Set<ItemLabelEnum> labels;

    @TableField(exist = false)
    private Map<ItemOperationEnum, ItemTypeAttribute> attributes;

    @TableField(exist = false)
    private Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffect>> effects;
}
