package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDo {

    @TableId
    private String id;

    private String owner;

    private ItemTypeEnum itemType;

    private Integer itemCount;

    private Integer life;

    private Integer level;

    private ItemPositionEnum position;

    @TableField(exist = false)
    private ItemTypeDo itemTypeObj;

}
