package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.bean.SpriteWithTypeBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

@Component
public class SpiderAgent implements SpriteAgent {
    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    public SpiderAgent(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
    }

    @Override
    public MoveBo act(SpriteDetailBo sprite) {
        assert sprite.getCache() != null;
        // 在视觉范围内寻找一个目标
        // 蜘蛛的攻击目标需要满足的条件（必须有主人，并且不是蜘蛛）
        SpriteWithTypeBo target = gameMapService.getValidTargetWithRandomForget(sprite, 0.15);
        if (target == null) {
            target = gameMapService.findAnyTargetInSight(sprite,
                    (s) -> s.getType() != SpriteTypeEnum.SPIDER && (s.getOwner() != null || s.getType() == SpriteTypeEnum.USER)
            );
        }
        if (target == null) {
            // 随机移动
            return MoveBo.randomMove(sprite).moveWithProb(0.15);
        }
        sprite.getCache().setTargetSpriteId(target.getId());
        return MoveBo.moveToSprite(target);
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.SPIDER;
    }
}
