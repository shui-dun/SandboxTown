package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.SpriteBo;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.service.SpriteActionService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import org.springframework.stereotype.Component;

@Component
public class DogAgent implements SpriteAgent {

    private final SpriteService spriteService;

    private final SpriteActionService spriteActionService;

    public DogAgent(SpriteService spriteService, SpriteActionService spriteActionService) {
        this.spriteService = spriteService;
        this.spriteActionService = spriteActionService;
    }

    @Override
    public MoveBo act(SpriteBo sprite) {
        assert sprite.getOnlineCache() != null;
        // 如果狗有目标精灵（并以一定概率忘记目标），那么狗就会攻击目标精灵
        SpriteBo target = spriteActionService.getValidTargetWithRandomForget(sprite, 0.2)
                .map(s -> spriteService.selectOnlineSpriteById(s.getId()))
                .orElse(null);
        if (target != null) {
            return MoveBo.moveToSprite(target);
        }
        // 如果狗有主人
        SpriteDo owner = spriteActionService.getValidOwner(sprite).orElse(null);
        if (owner != null) {
            // 如果主人有攻击目标，那么狗也以主人的攻击目标为攻击目标
            // 这里有一个有趣的特例：如果主人的攻击目标是狗，那么狗可能会自残
            // 具体触发条件如下：
            // 1. 主人攻击它养的狗a
            // 2. 于是狗a会攻击主人
            // 3. 主人的另一只狗狗b会攻击狗a
            // 4. 于是狗a和狗b相互攻击
            // 5. 如果狗a杀死了狗b，那么狗a接着可能会攻击自己
            SpriteDo ownerTarget = spriteActionService.getValidTarget(owner).orElse(null);
            if (ownerTarget != null) {
                sprite.setTargetSpriteId(ownerTarget.getId());
                return MoveBo.empty();
            }
            // 否则狗一定概率就跟着主人走
            return MoveBo.moveToPoint(owner.getX(), owner.getY()).keepDistance().moveWithProb(0.75);
        }
        // 一定概率随机移动
        return MoveBo.randomMove(sprite).moveWithProb(0.25);
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.DOG;
    }
}
