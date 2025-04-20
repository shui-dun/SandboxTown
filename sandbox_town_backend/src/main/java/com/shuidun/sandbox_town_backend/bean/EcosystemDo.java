package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EcosystemTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("ecosystem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcosystemDo {
    @TableId
    private String id;
    private EcosystemTypeEnum type;
    private double centerX;
    private double centerY;
    private double width;
    private double height;
}