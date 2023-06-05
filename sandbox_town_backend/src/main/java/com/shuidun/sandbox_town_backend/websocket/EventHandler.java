package com.shuidun.sandbox_town_backend.websocket;

import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.CharacterStatus;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.service.MapService;
import com.shuidun.sandbox_town_backend.service.CharacterService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Slf4j
@Component
public class EventHandler {
    // 事件类型 -> 处理函数
    private Map<EventEnum, BiFunction<String, Map<String, Object>, Void>> eventMap = new HashMap<>();

    // 角色坐标信息，保存在内存中，定期写入数据库
    private Map<String, Point> characterAxis = new ConcurrentHashMap<>();

    // 角色状态信息，保存在内存中，不写入数据库
    private Map<String, CharacterStatus> characterStatus = new ConcurrentHashMap<>();

    private final CharacterService characterService;

    private final MapService mapService;

    public void handle(EventMessage eventMessage) {
        try {
            eventMap.get(eventMessage.getType()).apply(eventMessage.getInitiator(), eventMessage.getData());
        } catch (Exception e) {
            log.error("handle event error", e);
        }
    }

    public EventHandler(CharacterService characterService, MapService mapService) {
        this.characterService = characterService;
        this.mapService = mapService;

        // 上线事件
        eventMap.put(EventEnum.ONLINE, (initiator, data) -> {
            return null;
        });

        // 下线事件
        eventMap.put(EventEnum.OFFLINE, (initiator, data) -> {
            // 将玩家坐标信息写入数据库
            characterService.updateCharacterAttribute(initiator, "x", characterAxis.get(initiator).getX());
            characterService.updateCharacterAttribute(initiator, "y", characterAxis.get(initiator).getY());
            // TO-DO: 将玩家宠物的坐标信息写入数据库
            return null;
        });

        // 告知坐标信息
        eventMap.put(EventEnum.COORDINATE, (initiator, data) -> {
            int x = NumUtils.toInt(data.get("x"));
            int y = NumUtils.toInt(data.get("y"));
            String id = data.get("id").toString();
            // TO-DO: 只能控制自己或者是自己的宠物或者公共npc
            // 如果是其他玩家或者是其他玩家的宠物，直接返回
            var position = new Point(x, y);
            // 更新坐标信息
            characterAxis.put(id, position);
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
            log.info("{}", data.get("dest_id"));
            int x0 = NumUtils.toInt(data.get("x0"));
            int y0 = NumUtils.toInt(data.get("y0"));
            int x1 = NumUtils.toInt(data.get("x1"));
            int y1 = NumUtils.toInt(data.get("y1"));
            String destId = data.get("dest_id") != null ? data.get("dest_id").toString() : null;
            // 更新玩家的坐标信息
            characterAxis.put(initiator, new Point(x0, y0));
            // 更新玩家的找到的路径
            // TO-DO: 每种角色的宽度和高度不一样，需要根据角色类型来获取
            List<Point> path = mapService.findPath(x0, y0, x1, y1, (int) (150 * 0.6), (int) (150 * 0.7),
                    destId != null ? destId.hashCode() : null);
            // 如果找不到路径，直接返回
            if (path == null) {
                return null;
            }
            // 更新玩家的状态
            characterStatus.put(initiator, CharacterStatus.FINDING_PATH);
            // 通知玩家移动
            Map<String, Object> result = new HashMap<>();
            result.put("id", initiator);
            result.put("speed", characterService.getCharacterInfoByID(initiator).getSpeed());
            result.put("path", DataCompressor.compressPath(path));
            result.put("dest_id", data.get("dest_id"));
            WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, result));
            return null;
        });

    }

}
