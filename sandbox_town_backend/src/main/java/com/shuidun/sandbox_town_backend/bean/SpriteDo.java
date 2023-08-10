package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private Integer visionRange;

    private Integer attackRange;

    private Integer X;

    private Integer Y;

    private Integer width;

    private Integer height;

    private String map;

    /** 以下是一些放在sprite_type表中的字段 */
    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String description;

    @TableField(exist = false)
    private Double widthRatio;

    @TableField(exist = false)
    private Double heightRatio;

    /**
     * 玩家最后的属性值等于原先的属性值加上增量（装备或手持装备导致的属性变化）
     * 但注意: 没有moneyInc、expInc、levelInc
     */
    @TableField(exist = false)
    private Integer hungerInc;

    @TableField(exist = false)
    private Integer hpInc;

    @TableField(exist = false)
    private Integer attackInc;

    @TableField(exist = false)
    private Integer defenseInc;

    @TableField(exist = false)
    private Integer speedInc;

    @TableField(exist = false)
    private Integer visionRangeInc;

    @TableField(exist = false)
    private Integer attackRangeInc;

    /** 效果列表 */
    @TableField(exist = false)
    private List<SpriteEffectDo> effects;

    /** 装备列表 */
    @TableField(exist = false)
    private List<ItemDo> equipments;

    /**
     * 以下是一些只放在缓存不放在数据库的字段
     */
    @TableField(exist = false)
    private double vx;

    @TableField(exist = false)
    private double vy;

    @TableField(exist = false)
    private long lastUpdateTime;

    @TableField(exist = false)
    private long lastInteractTime;

    @TableField(exist = false)
    private SpriteStatus status;
}
