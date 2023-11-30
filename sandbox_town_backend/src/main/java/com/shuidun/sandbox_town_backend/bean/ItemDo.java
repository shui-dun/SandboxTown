package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDo {

    @TableId
    private String id;

    private String owner;

    private ItemTypeEnum itemType;

    private Integer itemCount;

    private Integer life;

    private Integer level;

    private ItemPositionEnum position;

    public ItemDo(ItemDo other) {
        this.id = other.id;
        this.owner = other.owner;
        this.itemType = other.itemType;
        this.itemCount = other.itemCount;
        this.life = other.life;
        this.level = other.level;
        this.position = other.position;
    }
}
