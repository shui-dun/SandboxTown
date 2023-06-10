package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteType {

    @TableId
    private String type;

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

    private Integer basicWidth;

    private Integer basicHeight;
}
