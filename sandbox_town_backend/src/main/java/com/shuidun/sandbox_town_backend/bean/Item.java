package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    private String itemType;

    private Integer itemCount;

    private Integer life;

    private Integer level;

    private String position;

    @TableField(exist = false)
    private ItemType itemTypeBean;

    @TableField(exist = false)
    private Set<String> labels;

    // Map<operationName, itemTypeAttribute>
    @TableField(exist = false)
    private Map<String, ItemTypeAttribute> attributes;

    // Map<operationName, <effectName, itemTypeEffect>>
    @TableField(exist = false)
    private Map<String, Map<String, ItemTypeEffect>> effects;

}
