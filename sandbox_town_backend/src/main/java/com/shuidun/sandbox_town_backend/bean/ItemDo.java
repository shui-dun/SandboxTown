package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDo {

    @TableId
    @NonNull
    private String id;

    @NonNull
    private String owner;

    @NonNull
    private ItemTypeEnum itemType;

    @NonNull
    private Integer itemCount;

    @NonNull
    private Integer life;

    @NonNull
    private Integer level;

    @NonNull
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
