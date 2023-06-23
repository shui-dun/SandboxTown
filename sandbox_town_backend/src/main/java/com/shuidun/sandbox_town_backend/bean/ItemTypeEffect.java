package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type_effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTypeEffect {

    private String itemType;

    private String operation;

    private String effect;

    private Integer duration;
}
