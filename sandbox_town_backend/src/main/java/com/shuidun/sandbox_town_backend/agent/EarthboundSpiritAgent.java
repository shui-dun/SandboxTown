package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MapBitsPermissionsBo;
import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteBo;
import com.shuidun.sandbox_town_backend.enumeration.MapBitEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.service.SpriteActionService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EarthboundSpiritAgent implements SpriteAgent {

    private final SpriteService spriteService;

    private final SpriteActionService spriteActionService;

    public EarthboundSpiritAgent(SpriteService spriteService, SpriteActionService spriteActionService) {
        this.spriteService = spriteService;
        this.spriteActionService = spriteActionService;
    }

    @Override
    public MoveBo act(SpriteBo sprite) {
        assert sprite.getOnlineCache() != null;
        // 如果有目标
        SpriteBo target = spriteActionService.getValidTargetWithRandomForget(sprite, 0.25)
                .map(s -> spriteService.selectOnlineSpriteById(s.getId())).orElse(null);
        if (target != null) {
            return MoveBo.moveToSprite(target).moveWithProb(0.85);
        }
        // 如果有其他视野范围内的地缚灵有目标，则同样以这个目标为目标
        target = spriteActionService.findAllTargetsInSight(sprite, (s) ->
                        s.getType() == SpriteTypeEnum.EARTHBOUND_SPIRIT)
                .stream()
                .map(spriteActionService::getValidTarget)
                .flatMap(Optional::stream) // 将每个 Optional 对象转换为一个可能为空的流，然后将这些流合并起来
                .findAny()
                .map(s -> spriteService.selectOnlineSpriteById(s.getId()))
                .orElse(null);
        if (target != null) {
            sprite.getOnlineCache().setTargetSpriteId(target.getId());
            return MoveBo.moveToSprite(target).moveWithProb(0.85);
        }
        // 否则随机移动
        return MoveBo.randomMove(sprite).moveWithProb(0.25);
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.EARTHBOUND_SPIRIT;
    }

    /** 默认只能在墓碑周围移动 */
    private static final int DEFAULT_ALLOW = MapBitsPermissionsBo.mapBitArrayToInt(MapBitEnum.SURROUNDING_TOMBSTONE);

    /** 默认不能在希腊神庙周围移动 */
    private static final int DEFAULT_FORBID = MapBitsPermissionsBo.mapBitArrayToInt(MapBitEnum.SURROUNDING_GREEK_TEMPLE);

    @Override
    public MapBitsPermissionsBo mapBitsPermissions(SpriteBo sprite) {
        int obstacles = MapBitsPermissionsBo.DEFAULT_OBSTACLES;
        // 默认只能在墓碑周围移动
        int allow = DEFAULT_ALLOW;
        // 如果体力小于一定值，则允许在任意地方移动
        if (sprite.getHp() < 50) {
            allow = MapBitsPermissionsBo.DEFAULT_ALLOW;
        }
        // 默认不能在希腊神庙周围移动
        int forbid = DEFAULT_FORBID;
        // 如果等级大于一定值，则可以在希腊神庙周围移动
        if (sprite.getLevel() > 5) {
            forbid = MapBitsPermissionsBo.DEFAULT_FORBID;
        }
        return new MapBitsPermissionsBo(obstacles, allow, forbid);
    }
}
