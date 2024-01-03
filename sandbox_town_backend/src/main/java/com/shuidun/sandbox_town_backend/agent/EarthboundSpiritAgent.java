package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.bean.SpriteWithTypeBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EarthboundSpiritAgent implements SpriteAgent {

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    public EarthboundSpiritAgent(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
    }

    @Override
    public MoveBo act(SpriteDetailBo sprite) {
        assert sprite.getCache() != null;
        // 如果有目标
        SpriteWithTypeBo target = gameMapService.getValidTargetWithRandomForget(sprite, 0.25)
                .map(s -> spriteService.selectByIdWithType(s.getId())).orElse(null);
        if (target != null) {
            return MoveBo.moveToSprite(target).moveWithProb(0.85);
        }
        // 如果有其他视野范围内的地缚灵有目标，则同样以这个目标为目标
        target = gameMapService.findAllTargetsInSight(sprite, (s) ->
                        s.getType() == SpriteTypeEnum.EARTHBOUND_SPIRIT)
                .stream()
                .map(gameMapService::getValidTarget)
                .flatMap(Optional::stream) // 将每个 Optional 对象转换为一个可能为空的流，然后将这些流合并起来
                .findAny()
                .map(s -> spriteService.selectByIdWithType(s.getId()))
                .orElse(null);
        if (target != null) {
            sprite.getCache().setTargetSpriteId(target.getId());
            return MoveBo.moveToSprite(target).moveWithProb(0.85);
        }
        // 否则随机移动
        return MoveBo.randomMove(sprite).moveWithProb(0.25);
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.EARTHBOUND_SPIRIT;
    }
}
