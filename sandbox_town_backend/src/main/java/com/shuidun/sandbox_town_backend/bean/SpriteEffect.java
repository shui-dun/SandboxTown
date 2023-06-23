package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite_effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteEffect {

    private String sprite;

    private String effect;

    private Integer duration;
}
