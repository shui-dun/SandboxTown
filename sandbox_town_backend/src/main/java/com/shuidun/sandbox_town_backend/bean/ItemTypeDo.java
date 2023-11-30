package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeDo {

    @TableId
    private ItemTypeEnum id;

    private String name;

    private String description;

    private Integer basicPrice;

    private Integer rarity;

    private Integer durability;

    public ItemTypeDo(ItemTypeDo other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.basicPrice = other.basicPrice;
        this.rarity = other.rarity;
        this.durability = other.durability;
    }
}
