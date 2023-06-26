package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("store_item_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemType {

    private ItemTypeEnum itemType;

    private String store;

    private Integer count;

    private Integer price;
}
