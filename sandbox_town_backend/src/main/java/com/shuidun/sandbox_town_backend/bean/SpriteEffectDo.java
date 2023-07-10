package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite_effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteEffectDo {

    private String sprite;

    private EffectEnum effect;

    private Integer duration;
}
