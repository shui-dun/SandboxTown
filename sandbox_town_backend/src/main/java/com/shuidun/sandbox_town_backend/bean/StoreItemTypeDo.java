package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("store_item_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemTypeDo {

    private ItemTypeEnum itemType;

    private String store;

    private Integer count;

    private Integer price;

    @TableField(exist = false)
    private ItemTypeDo itemTypeObj;
}
