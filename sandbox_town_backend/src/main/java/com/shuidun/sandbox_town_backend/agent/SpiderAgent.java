package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.bean.SpriteWithTypeBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.service.SpriteActionService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

@Component
public class SpiderAgent implements SpriteAgent {
    private final SpriteService spriteService;

    private final SpriteActionService spriteActionService;

    public SpiderAgent(SpriteService spriteService, SpriteActionService spriteActionService) {
        this.spriteService = spriteService;
        this.spriteActionService = spriteActionService;
    }

    @Override
    public MoveBo act(SpriteDetailBo sprite) {
        assert sprite.getCache() != null;
        // 在视觉范围内寻找一个目标
        // 蜘蛛的攻击目标需要满足的条件（必须有主人，并且不是蜘蛛）
        SpriteWithTypeBo target = spriteActionService.getValidTargetWithRandomForget(sprite, 0.15)
                .map(s -> spriteService.selectByIdWithType(s.getId()))
                .orElse(null);
        if (target == null) {
            target = spriteActionService.findAnyTargetInSight(sprite,
                    (s) -> s.getType() != SpriteTypeEnum.SPIDER && (s.getOwner() != null || s.getType() == SpriteTypeEnum.USER)
            ).map(s -> spriteService.selectByIdWithType(s.getId())).orElse(null);
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
