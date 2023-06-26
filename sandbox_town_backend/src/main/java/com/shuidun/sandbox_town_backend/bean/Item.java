package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@TableName("item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @TableId
    private String id;

    private String owner;

    private ItemTypeEnum itemType;

    private Integer itemCount;

    private Integer life;

    private Integer level;

    private ItemPositionEnum position;

    @TableField(exist = false)
    private ItemType itemTypeBean;

    @TableField(exist = false)
    private Set<ItemLabelEnum> labels;

    @TableField(exist = false)
    private Map<ItemOperationEnum, ItemTypeAttribute> attributes;

    @TableField(exist = false)
    private Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffect>> effects;

}
