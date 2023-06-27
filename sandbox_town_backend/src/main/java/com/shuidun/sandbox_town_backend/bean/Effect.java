package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Effect {

    @TableId
    private EffectEnum id;

    private String name;

    private String description;
}
