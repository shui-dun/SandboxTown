package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@TableName("store_item_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemTypeDo {

    @NonNull
    private ItemTypeEnum itemType;

    @NonNull
    private String store;

    @NonNull
    private Integer count;

    @NonNull
    private Integer price;

    @TableField(exist = false)
    @Nullable
    private ItemTypeDo itemTypeObj;
}
