package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EcosystemTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("ecosystem_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcosystemTypeDo {
    @TableId
    private EcosystemTypeEnum id;
    private String name;
    private double basicWidth;
    private double basicHeight;
    private int rarity;
}