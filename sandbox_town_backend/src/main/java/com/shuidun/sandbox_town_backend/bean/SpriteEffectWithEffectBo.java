package com.shuidun.sandbox_town_backend.bean;

import lombok.*;
import org.springframework.lang.NonNull;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteEffectWithEffectBo extends SpriteEffectDo {
    @NonNull
    private EffectDo effectObj;
}
