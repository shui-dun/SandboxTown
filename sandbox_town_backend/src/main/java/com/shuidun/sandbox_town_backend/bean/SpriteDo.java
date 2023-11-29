package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

@TableName("sprite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteDo {

    @TableId
    @NonNull
    private String id;

    @NonNull
    private SpriteTypeEnum type;

    @Nullable
    private String owner;

    @NonNull
    private Integer money;

    @NonNull
    private Integer exp;

    @NonNull
    private Integer level;

    @NonNull
    private Integer hunger;

    @NonNull
    private Integer hp;

    @NonNull
    private Integer attack;

    @NonNull
    private Integer defense;

    @NonNull
    private Integer speed;

    @NonNull
    private Integer visionRange;

    @NonNull
    private Integer attackRange;

    @NonNull
    private Double X;

    @NonNull
    private Double Y;

    @NonNull
    private Double width;

    @NonNull
    private Double height;

    @NonNull
    private String map;

    /** 以下是Join sprite_type表的字段 */
    @TableField(exist = false)
    @Nullable
    private String name;

    @TableField(exist = false)
    @Nullable
    private String description;

    @TableField(exist = false)
    @Nullable
    private Double widthRatio;

    @TableField(exist = false)
    @Nullable
    private Double heightRatio;

    /**
     * 以下是查询精灵的装备和效果等信息后得到的字段
     * 玩家最后的属性值等于原先的属性值加上增量（装备或手持装备导致的属性变化）
     * 但注意: 没有moneyInc、expInc、levelInc
     */
    @TableField(exist = false)
    @Nullable
    private Integer hungerInc;

    @TableField(exist = false)
    @Nullable
    private Integer hpInc;

    @TableField(exist = false)
    @Nullable
    private Integer attackInc;

    @TableField(exist = false)
    @Nullable
    private Integer defenseInc;

    @TableField(exist = false)
    @Nullable
    private Integer speedInc;

    @TableField(exist = false)
    @Nullable
    private Integer visionRangeInc;

    @TableField(exist = false)
    @Nullable
    private Integer attackRangeInc;

    /** 效果列表 */
    @TableField(exist = false)
    @Nullable
    private List<SpriteEffectDo> effects;

    /** 装备列表 */
    @TableField(exist = false)
    @Nullable
    private List<ItemDo> equipments;

    public SpriteDo(String id, SpriteTypeEnum type, @Nullable String owner, Integer money, Integer exp, Integer level, Integer hunger, Integer hp, Integer attack, Integer defense, Integer speed, Integer visionRange, Integer attackRange, Double x, Double y, Double width, Double height, String map) {
        this.id = id;
        this.type = type;
        this.owner = owner;
        this.money = money;
        this.exp = exp;
        this.level = level;
        this.hunger = hunger;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.visionRange = visionRange;
        this.attackRange = attackRange;
        X = x;
        Y = y;
        this.width = width;
        this.height = height;
        this.map = map;
    }
}
