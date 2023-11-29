package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("sprite_effect")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteEffectDo {

    @NonNull
    private String sprite;

    @NonNull
    private EffectEnum effect;

    @NonNull
    private Integer duration;

    @NonNull
    private Long expire;

    public SpriteEffectDo(SpriteEffectDo other) {
        this.sprite = other.sprite;
        this.effect = other.effect;
        this.duration = other.duration;
        this.expire = other.expire;
    }
}
