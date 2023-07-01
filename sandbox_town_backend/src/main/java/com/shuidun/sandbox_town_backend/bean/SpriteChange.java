package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;

@Data
public class SpriteChange {
    private SpriteAttributeChange spriteAttributeChange = new SpriteAttributeChange();

    private SpriteEffectChange spriteEffectChange = new SpriteEffectChange();
}
