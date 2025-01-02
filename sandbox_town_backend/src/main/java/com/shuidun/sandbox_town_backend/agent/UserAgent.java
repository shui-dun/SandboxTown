package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import org.springframework.stereotype.Component;

@Component
public class UserAgent implements SpriteAgent {
    @Override
    public MoveBo act(SpriteBo sprite) {
        // 一定概率忘记目标（否则玩家的狗会一直追着攻击玩家的目标）
        if (GameCache.random.nextDouble() > 0.8) {
            sprite.setTargetSpriteId(null);
        }
        return MoveBo.empty();
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.USER;
    }
}
