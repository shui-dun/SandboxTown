package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;

@Data
public class SpriteChangeVo {
    private SpriteAttributeChangeVo spriteAttributeChange = new SpriteAttributeChangeVo();

    private SpriteEffectChangeVo spriteEffectChange = new SpriteEffectChangeVo();
}
