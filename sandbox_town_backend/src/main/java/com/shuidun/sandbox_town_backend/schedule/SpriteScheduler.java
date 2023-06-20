package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.SpriteCache;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.websocket.WSManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class SpriteScheduler {

    // 类型到函数的映射
    private final Map<String, Function<Sprite, Void>> typeToFunction = new HashMap<>();

    private final SpriteService spriteService;

    public SpriteScheduler(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        // 狗的处理函数
        typeToFunction.put("dog", sprite -> {
            // 获得狗的主人
            String owner = sprite.getOwner();
            // 如果狗没有主人
            if (owner == null) {
                // 随机移动
                if (GameCache.random.nextDouble() < 0.5) {
                    return null;
                }
                double randomVx = sprite.getSpeed() * (Math.random() - 0.5);
                double randomVy = sprite.getSpeed() * (Math.random() - 0.5);
                WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.COORDINATE, Map.of(
                        "id", sprite.getId(),
                        "x", sprite.getX(),
                        "y", sprite.getY(),
                        "vx", randomVx,
                        "vy", randomVy
                )));
            } else {
                // 如果狗有主人，那么狗一定概率就跟着主人走
                if (GameCache.random.nextDouble() < 0.6) {
                    return null;
                }
                SpriteCache ownerSprite = GameCache.spriteCacheMap.get(owner);
                log.info("ownerSprite: {}", ownerSprite);
                if (ownerSprite == null) {
                    return null;
                }
                double distance = gameMapService.calcDistance(sprite.getX(), sprite.getY(), ownerSprite.getX(), ownerSprite.getY());
                log.info("distance: {}", distance);
                // 如果距离过远，那就不跟随
                if (distance > 1000) {
                    return null;
                }
                // 寻找路径
                var path = gameMapService.findPath(sprite.getX(), sprite.getY(), ownerSprite.getX(), ownerSprite.getY(), (int) (sprite.getWidth() * 0.65), (int) (sprite.getHeight() * 0.75), null);
                log.info("path: {}", path);
                // 如果找不到路径，那就不跟随
                if (path == null) {
                    return null;
                }
                // 如果距离过近，那就不跟随
                int minLen = 7;
                if (path.size() < minLen) {
                    return null;
                }
                // 去掉后面一段
                path = path.subList(0, path.size() - minLen);
                // 发送移动消息
                Map<String, Object> result = new HashMap<>();
                result.put("id", sprite.getId());
                result.put("speed", sprite.getSpeed());
                result.put("path", DataCompressor.compressPath(path));
                result.put("dest_id", null);
                WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, result));
            }
            return null;
        });

    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void schedule() {
        // 遍历所有角色
        for (String id : GameCache.spriteCacheMap.keySet()) {
            // 得到其角色
            Sprite sprite = spriteService.selectById(id);
            // 调用对应的处理函数
            var func = typeToFunction.get(sprite.getType());
            if (func != null) {
                func.apply(sprite);
            }
        }
    }
}
