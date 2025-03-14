package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("fusion_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FusionMaterialDo {
    @TableId
    private Integer id;
    private Integer fusionId;
    private ItemTypeEnum itemName;
    private Integer quantity;
}