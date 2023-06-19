package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.Sprite;
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


        // 下线事件
        eventMap.put(EventEnum.OFFLINE, (initiator, data) -> {
            // 读取角色的所有宠物
            List<Sprite> pets = spriteService.selectByOwner(initiator);
            // 删除角色以及其宠物坐标等信息
            GameCache.spriteAxis.remove(initiator);
            pets.forEach(pet -> GameCache.spriteAxis.remove(pet.getId()));
            // 通知其他玩家
            WSResponse wsResponse = new WSResponse(WSResponseEnum.OFFLINE, Map.of("id", initiator));
            WSManager.sendMessageToAllUsers(wsResponse);
            return null;
        });

        // 告知坐标信息
        eventMap.put(EventEnum.COORDINATE, (initiator, data) -> {
            int x = NumUtils.toInt(data.get("x"));
            int y = NumUtils.toInt(data.get("y"));
            double vx = NumUtils.toDouble(data.get("vx"));
            double vy = NumUtils.toDouble(data.get("vy"));
            var position = new Point(x, y);
            String id = data.get("id").toString();
            // 如果是第一次通报坐标信息，说明刚上线
            boolean isFirstTime = !GameCache.spriteAxis.containsKey(id);

            // TO-DO: 只能控制自己或者是自己的宠物或者公共npc
            // 如果是其他玩家或者是其他玩家的宠物，直接返回
            // 更新坐标信息
            GameCache.spriteAxis.put(id, position);
            // 广播给其他玩家
            // 如果是第一次通报坐标信息，说明刚上线，需要广播上线信息
            WSResponse response;
            if (isFirstTime) {
                // 广播上线信息
                response = new WSResponse(WSResponseEnum.ONLINE, JSONObject.parseObject(JSON.toJSONString(spriteService.getSpriteInfoByID(id)), Map.class));
            } else { // 如果不是第一次通报坐标信息，只需广播坐标信息
                response = new WSResponse(WSResponseEnum.COORDINATE, Map.of(
                        "id", id,
                        "x", x,
                        "y", y,
                        "vx", vx,
                        "vy", vy
                ));
            }
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
