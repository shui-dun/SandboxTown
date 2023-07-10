package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;

/**
 * 记录精灵的属性变化，只有玩家自己发生变化才会收到通知
 */
@Data
public class SpriteAttributeChangeVo {
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

    // 设置原先的属性值
    public void setOriginal(SpriteDo original) {
        this.id = original.getId();
        this.money = original.getMoney();
        this.exp = original.getExp();
        this.level = original.getLevel();
        this.hunger = original.getHunger();
        this.hp = original.getHp();
        this.attack = original.getAttack();
        this.defense = original.getDefense();
        this.speed = original.getSpeed();
    }

    // 设置增量
    public void setChanged(SpriteDo changed) {
        this.id = changed.getId();
        this.moneyInc = changed.getMoney() - this.money;
        this.expInc = changed.getExp() - this.exp;
        this.levelInc = changed.getLevel() - this.level;
        this.hungerInc = changed.getHunger() - this.hunger;
        this.hpInc = changed.getHp() - this.hp;
        this.attackInc = changed.getAttack() - this.attack;
        this.defenseInc = changed.getDefense() - this.defense;
        this.speedInc = changed.getSpeed() - this.speed;
    }
}
