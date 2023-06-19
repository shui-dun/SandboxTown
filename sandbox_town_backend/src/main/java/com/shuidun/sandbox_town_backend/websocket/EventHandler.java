package com.shuidun.sandbox_town_backend.websocket;

import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@Component
public class EventHandler {
    // 事件类型 -> 处理函数
    private Map<EventEnum, BiFunction<String, Map<String, Object>, Void>> eventMap = new HashMap<>();

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    public void handle(EventMessage eventMessage) {
        try {
            // 如果类型是空，就不处理
            if (eventMessage.getType() == null) {
                return;
            }
            eventMap.get(eventMessage.getType()).apply(eventMessage.getInitiator(), eventMessage.getData());
        } catch (Exception e) {
            log.error("handle {} event error", eventMessage, e);
        }
    }

    public EventHandler(SpriteService spriteService, GameMapService gameMapService) {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;


        // 下线事件
        eventMap.put(EventEnum.OFFLINE, (initiator, data) -> {
            // TO-DO: 删除角色以及其宠物坐标等信息
            return null;
        });

        // 告知坐标信息
        eventMap.put(EventEnum.COORDINATE, (initiator, data) -> {
            // TO-DO: 如果是第一次通报坐标信息，说明刚上线
            // 将角色信息加入缓存
            // 并将该信息发给其他所有在线的玩家
            int x = NumUtils.toInt(data.get("x"));
            int y = NumUtils.toInt(data.get("y"));
            String id = data.get("id").toString();
            // TO-DO: 只能控制自己或者是自己的宠物或者公共npc
            // 如果是其他玩家或者是其他玩家的宠物，直接返回
            var position = new Point(x, y);
            // 更新坐标信息
            GameCache.spriteAxis.put(id, position);
            // 广播给其他玩家
            var response = new WSResponse(WSResponseEnum.COORDINATE, Map.of(
                    "id", id,
                    "x", x,
                    "y", y
            ));
            WSManager.sendMessageToAllUsers(response);
            return null;
        });

        // 想要移动
        eventMap.put(EventEnum.MOVE, (initiator, data) -> {
            log.info("MOVE: {}", data);
            int x0 = NumUtils.toInt(data.get("x0"));
            int y0 = NumUtils.toInt(data.get("y0"));
            int x1 = NumUtils.toInt(data.get("x1"));
            int y1 = NumUtils.toInt(data.get("y1"));
            String destId = data.get("dest_id") != null ? data.get("dest_id").toString() : null;
            // 更新玩家的坐标信息
            GameCache.spriteAxis.put(initiator, new Point(x0, y0));
            // 更新玩家的找到的路径
            // TO-DO: 每种角色的宽度和高度不一样，需要根据角色类型来获取
            List<Point> path = gameMapService.findPath(x0, y0, x1, y1, (int) (150 * 0.65), (int) (150 * 0.75),
                    destId != null ? destId.hashCode() : null);
            // 如果找不到路径，直接返回
            if (path == null) {
                return null;
            }
            // 更新玩家的状态
            GameCache.spriteStatus.put(initiator, SpriteStatus.FINDING_PATH);
            // 通知玩家移动
            Map<String, Object> result = new HashMap<>();
            result.put("id", initiator);
            result.put("speed", spriteService.getSpriteInfoByID(initiator).getSpeed());
            result.put("path", DataCompressor.compressPath(path));
            result.put("dest_id", data.get("dest_id"));
            WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, result));
            return null;
        });

    }

}
