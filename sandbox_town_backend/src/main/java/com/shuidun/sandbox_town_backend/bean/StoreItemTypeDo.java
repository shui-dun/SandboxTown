package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("store_item_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeDo {

    private ItemTypeEnum itemType;

    private String store;

    private Integer count;

    private Integer price;

    public StoreItemTypeDo(StoreItemTypeDo other) {
        this.itemType = other.itemType;
        this.store = other.store;
        this.count = other.count;
        this.price = other.price;
    }
}
