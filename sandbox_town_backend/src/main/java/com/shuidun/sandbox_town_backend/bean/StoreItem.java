package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("store_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItem {

    private String item;

    private String store;

    private Integer count;

    private Integer maxCount;

    private Integer price;
}
