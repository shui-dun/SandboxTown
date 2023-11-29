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

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteWithTypeBo {
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
    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private Double widthRatio;

    @NonNull
    private Double heightRatio;
}
