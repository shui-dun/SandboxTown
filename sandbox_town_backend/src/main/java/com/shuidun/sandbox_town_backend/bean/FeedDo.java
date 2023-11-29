package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("feed")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedDo {

    @NonNull
    private SpriteTypeEnum spriteType;

    @NonNull
    private ItemTypeEnum itemType;

    @NonNull
    private Double tameProb;

    @NonNull
    private Integer expInc;

    @NonNull
    private Integer hungerInc;
}
