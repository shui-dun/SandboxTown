package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * 记录精灵的属性变化，只有玩家自己发生变化才会收到通知
 */
@Data
@NoArgsConstructor
public class SpriteAttributeChangeVo {
    /** 精灵id */
    @NonNull
    private String id;

    @NonNull
    private Integer money;

    /** 这些增量表示的是变化后的值减去变化前的值 */
    @NonNull
    private Integer moneyInc;

    @NonNull
    private Integer exp;

    @NonNull
    private Integer expInc;

    @NonNull
    private Integer level;

    @NonNull
    private Integer levelInc;

    @NonNull
    private Integer hunger;

    @NonNull
    private Integer hungerInc;

    @NonNull
    private Integer hp;

    @NonNull
    private Integer hpInc;

    @NonNull
    private Integer attack;

    @NonNull
    private Integer attackInc;

    @NonNull
    private Integer defense;

    @NonNull
    private Integer defenseInc;

    @NonNull
    private Integer speed;

    @NonNull
    private Integer speedInc;

    @NonNull
    private Integer visionRange;

    @NonNull
    private Integer visionRangeInc;

    @NonNull
    private Integer attackRange;

    @NonNull
    private Integer attackRangeInc;

    /** 设置原先的属性值 */
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
        this.visionRange = original.getVisionRange();
        this.attackRange = original.getAttackRange();
    }

    /**
     * 设置增量
     *
     * @param changed 变化后的精灵
     * @return 是否有变化
     */
    public boolean setChanged(SpriteDo changed) {
        this.id = changed.getId();
        this.moneyInc = changed.getMoney() - this.money;
        this.expInc = changed.getExp() - this.exp;
        this.levelInc = changed.getLevel() - this.level;
        this.hungerInc = changed.getHunger() - this.hunger;
        this.hpInc = changed.getHp() - this.hp;
        this.attackInc = changed.getAttack() - this.attack;
        this.defenseInc = changed.getDefense() - this.defense;
        this.speedInc = changed.getSpeed() - this.speed;
        this.visionRangeInc = changed.getVisionRange() - this.visionRange;
        this.attackRangeInc = changed.getAttackRange() - this.attackRange;
        return moneyInc != 0 || expInc != 0 || levelInc != 0 || hungerInc != 0 || hpInc != 0 || attackInc != 0 || defenseInc != 0 || speedInc != 0 || visionRangeInc != 0 || attackRangeInc != 0;
    }
}
