package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录精灵的效果变化，只有玩家自己发生变化，
 * 或者虽然是其他精灵的变化，但会引起前端动画变化才会收到通知
 */
@Data
public class SpriteEffectChangeVo {
    /** 精灵id */
    private String id;

    private List<EffectEnum> startEffects = new ArrayList<>();

    private List<EffectEnum> endEffects = new ArrayList<>();
}
