package com.shuidun.sandbox_town_backend.agent;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
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
    public void act(SpriteDetailBo sprite) {
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
                return;
            }
            // 如果距离过远（视野之外），那就不跟随
            if (!gameMapService.isInSight(sprite, target.getX(), target.getY())) {
                return;
            }
            // 寻找路径
            var path = gameMapService.findPath(sprite, target.getX(), target.getY(), null, target);
            // 如果找不到路径，那就不前往
            if (path.isEmpty()) {
                return;
            }
            // 发送移动消息
            WSMessageSender.addResponse(new WSResponseVo(
                    WSResponseEnum.MOVE,
                    new MoveVo(
                            sprite.getId(),
                            sprite.getSpeed() + sprite.getSpeedInc(),
                            DataCompressor.compressPath(path),
                            null,
                            targetId,
                            GameCache.random.nextInt()
                    )
            ));
        } else {
            // 如果狗没有主人
            if (owner == null) {
                // 随机移动
                if (GameCache.random.nextDouble() < 0.5) {
                    return;
                }
                var randomVelocity = gameMapService.randomVelocity(sprite);
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(sprite.getId(), sprite.getX(), sprite.getY(), randomVelocity.getFirst(), randomVelocity.getSecond())));
            } else {
                // 如果狗的主人在线
                SpriteCache ownerSprite = spriteService.getSpriteCache(owner);
                if (ownerSprite == null) {
                    return;
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
                } else {
                    // 否则狗一定概率就跟着主人走
                    if (GameCache.random.nextDouble() < 0.6) {
                        return;
                    }
                    // 如果距离过远（视野之外），那就不跟随
                    if (!gameMapService.isInSight(sprite, ownerSprite.getX(), ownerSprite.getY())) {
                        return;
                    }
                    // 寻找路径，但保持一定距离
                    var path = gameMapService.findPathNotTooClose(sprite, ownerSprite.getX(), ownerSprite.getY(), null, null);
                    // 如果找不到路径，那就不跟随
                    if (path.isEmpty()) {
                        return;
                    }
                    // 发送移动消息
                    WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, new MoveVo(sprite.getId(), sprite.getSpeed() + sprite.getSpeedInc(), DataCompressor.compressPath(path), null, null, null)));
                }
            }
        }
    }

    @Override
    public SpriteTypeEnum getType() {
        return SpriteTypeEnum.DOG;
    }
}
