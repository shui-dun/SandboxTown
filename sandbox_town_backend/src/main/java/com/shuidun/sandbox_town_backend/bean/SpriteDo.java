package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

    public SpriteDo(SpriteDo other) {
        this.id = other.id;
        this.type = other.type;
        this.owner = other.owner;
        this.money = other.money;
        this.exp = other.exp;
        this.level = other.level;
        this.hunger = other.hunger;
        this.hp = other.hp;
        this.attack = other.attack;
        this.defense = other.defense;
        this.speed = other.speed;
        this.visionRange = other.visionRange;
        this.attackRange = other.attackRange;
        this.X = other.X;
        this.Y = other.Y;
        this.width = other.width;
        this.height = other.height;
        this.map = other.map;
    }
}
