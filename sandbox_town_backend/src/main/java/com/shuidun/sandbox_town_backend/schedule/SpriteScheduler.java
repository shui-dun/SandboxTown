package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.SpriteService;
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

    public SpriteScheduler(SpriteService spriteService) {
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
                // TO-DO: 如果狗有主人，那么狗就跟着主人走
            }
            return null;
        });

    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void schedule() {
        // 遍历所有角色
        for (String id : GameCache.spriteAxis.keySet()) {
            // 得到其角色
            Sprite sprite = spriteService.getSpriteInfoByID(id);
            // 调用对应的处理函数
            var func = typeToFunction.get(sprite.getType());
            if (func != null) {
                func.apply(sprite);
            }
        }
    }
}
