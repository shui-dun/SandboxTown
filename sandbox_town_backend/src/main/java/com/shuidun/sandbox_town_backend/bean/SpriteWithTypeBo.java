package com.shuidun.sandbox_town_backend.bean;

import lombok.*;
import org.springframework.lang.NonNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteWithTypeBo extends SpriteDo {

    /** 以下是Join sprite_type表的字段 */
    private String name;

    private String description;

    private Double widthRatio;

    private Double heightRatio;

    public SpriteWithTypeBo(SpriteDo spriteDo, String name, String description, Double widthRatio, Double heightRatio) {
        super(spriteDo);
        this.name = name;
        this.description = description;
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
    }

    public SpriteWithTypeBo(SpriteWithTypeBo other) {
        super(other);
        this.name = other.name;
        this.description = other.description;
        this.widthRatio = other.widthRatio;
        this.heightRatio = other.heightRatio;
    }
}
