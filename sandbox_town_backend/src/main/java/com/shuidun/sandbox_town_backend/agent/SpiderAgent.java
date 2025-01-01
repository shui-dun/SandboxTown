package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MapBitsPermissionsBo;
import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteBo;
import com.shuidun.sandbox_town_backend.enumeration.MapBitEnum;
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
    public MoveBo act(SpriteBo sprite) {
        assert sprite.getOnlineCache() != null;
        // 在视觉范围内寻找一个目标
        // 蜘蛛的攻击目标需要满足的条件（必须有主人，并且不是蜘蛛）
        SpriteWithTypeBo target = spriteActionService.getValidTargetWithRandomForget(sprite, 0.15)
                .map(s -> spriteService.selectById(s.getId()))
                .orElse(null);
        if (target == null) {
            target = spriteActionService.findAnyTargetInSight(sprite,
                    (s) -> s.getType() != SpriteTypeEnum.SPIDER && (s.getOwner() != null || s.getType() == SpriteTypeEnum.USER)
            ).map(s -> spriteService.selectById(s.getId())).orElse(null);
        }
        if (target == null) {
            // 随机移动
            return MoveBo.randomMove(sprite).moveWithProb(0.15);
        }
        sprite.getOnlineCache().setTargetSpriteId(target.getId());
        return MoveBo.moveToSprite(target);
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.SPIDER;
    }

    /** 默认不能在希腊神庙周围移动 */
    private static final int DEFAULT_FORBID = MapBitsPermissionsBo.mapBitArrayToInt(MapBitEnum.SURROUNDING_GREEK_TEMPLE);

    @Override
    public MapBitsPermissionsBo mapBitsPermissions(SpriteBo sprite) {
        int obstacles = MapBitsPermissionsBo.DEFAULT_OBSTACLES;
        int allow = MapBitsPermissionsBo.DEFAULT_ALLOW;
        // 默认不能在希腊神庙周围移动
        int forbid = DEFAULT_FORBID;
        // 如果等级大于一定值，则可以在希腊神庙周围移动
        if (sprite.getLevel() > 5) {
            forbid = MapBitsPermissionsBo.DEFAULT_FORBID;
        }
        return new MapBitsPermissionsBo(obstacles, allow, forbid);
    }
}
