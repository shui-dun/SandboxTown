package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

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
        Predicate<SpriteDo> condition = (s) -> s.getType() != SpriteTypeEnum.SPIDER
                && (s.getOwner() != null || s.getType() == SpriteTypeEnum.USER);
        String finalTargetId;
        String originalTargetId = sprite.getCache().getTargetSpriteId();
        SpriteCache originalTarget = originalTargetId == null ? null : spriteService.getSpriteCache(originalTargetId);
        // 如果蜘蛛没有目标，或者目标已经不存在，或者目标不在线
        if (originalTargetId == null
                || originalTarget == null) {
            finalTargetId = gameMapService.findAnyTargetInSight(sprite, condition).map(SpriteDo::getId).orElse(null);
        } else {
            // 判断目标是否过远
            if (gameMapService.isInSight(sprite, originalTarget.getX(), originalTarget.getY())) {
                // 有一定概率忘记目标
                if (GameCache.random.nextDouble() > 0.9) {
                    finalTargetId = gameMapService.findAnyTargetInSight(sprite, condition).map(SpriteDo::getId).orElse(null);
                } else {
                    finalTargetId = originalTargetId;
                }
            } else {
                finalTargetId = gameMapService.findAnyTargetInSight(sprite, condition).map(SpriteDo::getId).orElse(null);
            }
        }
        SpriteWithTypeBo finalTarget = finalTargetId == null ? null
                : spriteService.selectByIdWithType(finalTargetId);
        if (finalTarget == null) {
            // 随机移动
            if (GameCache.random.nextDouble() < 0.85) {
                return MoveBo.empty();
            }
            return Agents.randomMove(sprite);
        }
        sprite.getCache().setTargetSpriteId(finalTargetId);
        return MoveBo.moveToSprite(finalTarget);
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.SPIDER;
    }
}
