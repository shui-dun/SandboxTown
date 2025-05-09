package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@TableName("sprite")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteDo {

    @TableId
    private String id;

    private SpriteTypeEnum type;

    @Nullable
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

    private Double X;

    private Double Y;

    private Double width;

    private Double height;

    private String map;
}
