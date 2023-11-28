package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("feed")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedDo {

    private SpriteTypeEnum spriteType;

    private ItemTypeEnum itemType;

    private Double tameProb;

    private Integer expInc;

    private Integer hungerInc;
}
