package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;

/**
 * 该接口定义了游戏中不同类型精灵行为的基础框架。
 * 每种精灵类型（如玩家、狗、蜘蛛等）都应该有一个对应的实现类，
 * 在这个实现类中具体定义了该类型精灵的行为逻辑。
 */
public interface SpriteAgent {
    /**
     * act 方法是每个精灵行动逻辑的核心。
     * 它定义了当游戏循环每次迭代时，精灵应该执行的操作。
     */
    void act(SpriteDetailBo sprite);

    /**
     * 得到精灵的类型
     */
    SpriteTypeEnum getType();
}
