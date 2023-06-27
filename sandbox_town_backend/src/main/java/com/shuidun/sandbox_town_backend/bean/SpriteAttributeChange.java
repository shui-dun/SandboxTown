package com.shuidun.sandbox_town_backend.bean;

/**
 * 记录精灵的属性变化，只有玩家自己发生变化才会收到通知
 */
public class SpriteAttributeChange {
    // 精灵id
    private String id;

    private Integer money;

    private Integer moneyInc;

    private Integer exp;

    private Integer expInc;

    private Integer level;

    private Integer levelInc;

    private Integer hunger;

    private Integer hungerInc;

    private Integer hp;

    private Integer hpInc;

    private Integer attack;

    private Integer attackInc;

    private Integer defense;

    private Integer defenseInc;

    private Integer speed;

    private Integer speedInc;
}
