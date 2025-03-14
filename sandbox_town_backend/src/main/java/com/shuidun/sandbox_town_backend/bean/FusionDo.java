package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("fusion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FusionDo {
    @TableId
    private Integer id;
    private ItemTypeEnum resultItemId;
}