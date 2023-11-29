package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Set;

@TableName("item_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemTypeDo {

    @TableId
    @NonNull
    private ItemTypeEnum id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private Integer basicPrice;

    @NonNull
    private Integer rarity;

    @NonNull
    private Integer durability;
}
