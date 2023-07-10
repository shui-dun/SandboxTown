package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteDo {

    @TableId
    private String id;

    private SpriteTypeEnum type;

    private String owner;

    private Integer money;

    private Integer exp;

    private Integer level;

    private Integer hunger;

    private Integer hp;

    private Integer attack;

    private Integer defense;

    private Integer speed;

    private Integer X;

    private Integer Y;

    private Integer width;

    private Integer height;

    private String map;

    // 描述信息
    @TableField(exist = false)
    private String description;

    // 以下是一些只放在缓存不放在数据库的字段
    @TableField(exist = false)
    private double vx;

    @TableField(exist = false)
    private double vy;

    @TableField(exist = false)
    private SpriteStatus status;
}
