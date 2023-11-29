package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("sprite_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteTypeDo {

    @TableId
    @NonNull
    private SpriteTypeEnum type;

    @NonNull
    private String name;

    @NonNull
    private String description;

    @NonNull
    private Integer basicPrice;

    @NonNull
    private Integer basicMoney;

    @NonNull
    private Integer basicExp;

    @NonNull
    private Integer basicLevel;

    @NonNull
    private Integer basicHunger;

    @NonNull
    private Integer basicHp;

    @NonNull
    private Integer basicAttack;

    @NonNull
    private Integer basicDefense;

    @NonNull
    private Integer basicSpeed;

    @NonNull
    private Double basicWidth;

    @NonNull
    private Double basicHeight;

    @NonNull
    private Integer basicVisionRange;

    @NonNull
    private Integer basicAttackRange;

    @NonNull
    private Double widthRatio;

    @NonNull
    private Double heightRatio;
}
