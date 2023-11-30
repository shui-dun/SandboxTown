package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("victory_item_reward")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictoryItemRewardDo {

    private SpriteTypeEnum spriteType;

    private ItemTypeEnum itemType;

    private Integer minCount;

    private Integer maxCount;
}
