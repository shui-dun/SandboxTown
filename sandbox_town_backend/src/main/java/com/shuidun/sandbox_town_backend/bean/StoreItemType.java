package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("store_item_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemType {

    @TableId
    private String itemType;

    @TableId
    private String store;

    private Integer count;

    private Integer price;
}
