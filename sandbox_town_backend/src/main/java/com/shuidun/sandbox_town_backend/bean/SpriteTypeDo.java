package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteTypeDo {

    @TableId
    private SpriteTypeEnum type;

    private String name;

    private String description;

    private Integer basicPrice;

    private Integer basicMoney;

    private Integer basicExp;

    private Integer basicLevel;

    private Integer basicHunger;

    private Integer basicHp;

    private Integer basicAttack;

    private Integer basicDefense;

    private Integer basicSpeed;

    private Double basicWidth;

    private Double basicHeight;

    private Integer basicVisionRange;

    private Integer basicAttackRange;

    private Double widthRatio;

    private Double heightRatio;
}
