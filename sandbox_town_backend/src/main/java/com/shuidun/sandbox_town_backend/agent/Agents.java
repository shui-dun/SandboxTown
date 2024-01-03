package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.mixin.GameCache;

/**
 * Agents 类作为一个工具类，提供了一系列静态方法，
 * 这些方法定义了游戏中各种精灵共享的行为和操作。可被不同类型的 SpriteAgent 实现所调用
 */
public class Agents {
    /** 精灵随机移动 */
    public static MoveBo randomMove(SpriteDetailBo sprite) {
        int randX = (sprite.getSpeed() + sprite.getSpeedInc()) * (GameCache.random.nextInt(11) - 5);
        int randY = (sprite.getSpeed() + sprite.getSpeedInc()) * (GameCache.random.nextInt(11) - 5);
        return MoveBo.moveToPoint(sprite.getX() + randX, sprite.getY() + randY);
    }
}
