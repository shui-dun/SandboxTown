package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteCache;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

@Component
public class DogAgent implements SpriteAgent {

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    public DogAgent(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
    }

    @Override
    public MoveBo act(SpriteDetailBo sprite) {
        assert sprite.getCache() != null;
        // 获得狗的主人
        String owner = sprite.getOwner();
        // 如果狗有目标精灵
        String targetId = sprite.getCache().getTargetSpriteId();
        if (targetId != null) {
            SpriteDetailBo target = spriteService.selectByIdWithDetail(targetId);
            // 如果目标精灵不存在或者不在线，那就不跟随
            // 有一定概率即使目标精灵存在，也取消跟随目标
            if (target == null
                    || target.getCache() == null
                    || GameCache.random.nextDouble() > 0.8) {
                sprite.getCache().setTargetSpriteId(null);
                return MoveBo.empty();
            }
            // 如果距离过远（视野之外），那就不跟随
            if (!gameMapService.isInSight(sprite, target.getX(), target.getY())) {
                return MoveBo.empty();
            }
            return MoveBo.moveToSprite(target);
        } else {
            // 如果狗没有主人
            if (owner == null) {
                // 随机移动
                if (GameCache.random.nextDouble() < 0.75) {
                    return MoveBo.empty();
                }
                return Agents.randomMove(sprite);
            } else {
                // 如果狗的主人在线
                SpriteCache ownerSprite = spriteService.getSpriteCache(owner);
                if (ownerSprite == null) {
                    return MoveBo.empty();
                }
                // 如果主人有攻击目标，那么狗也以主人的攻击目标为攻击目标
                // 这里有一个有趣的特例：如果主人的攻击目标是狗，那么狗可能会自残
                // 具体触发条件如下：
                // 1. 主人攻击它养的狗a
                // 2. 于是狗a会攻击主人
                // 3. 主人的另一只狗狗b会攻击狗a
                // 4. 于是狗a和狗b相互攻击
                // 5. 如果狗a杀死了狗b，那么狗a接着可能会攻击自己
                String ownerTargetId = ownerSprite.getTargetSpriteId();
                if (ownerTargetId != null && spriteService.getSpriteCache(ownerTargetId) != null) {
                    sprite.getCache().setTargetSpriteId(ownerTargetId);
                    return MoveBo.empty();
                } else {
                    // 否则狗一定概率就跟着主人走
                    if (GameCache.random.nextDouble() < 0.6) {
                        return MoveBo.empty();
                    }
                    // 如果距离过远（视野之外），那就不跟随
                    if (!gameMapService.isInSight(sprite, ownerSprite.getX(), ownerSprite.getY())) {
                        return MoveBo.empty();
                    }
                    return MoveBo.moveToPoint(ownerSprite.getX(), ownerSprite.getY()).keepDistance();
                }
            }
        }
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.DOG;
    }
}
