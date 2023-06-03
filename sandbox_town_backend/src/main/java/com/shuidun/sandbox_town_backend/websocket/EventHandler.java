package com.shuidun.sandbox_town_backend.websocket;

import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.CharacterStatus;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.service.MapService;
import com.shuidun.sandbox_town_backend.service.PlayerService;
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

    private final PlayerService playerService;

    private final MapService mapService;

    public void handle(EventMessage eventMessage) {
        try {
            eventMap.get(eventMessage.getType()).apply(eventMessage.getInitiator(), eventMessage.getData());
        } catch (Exception e) {
            log.error("handle event error", e);
        }
    }

    public EventHandler(PlayerService playerService, MapService mapService) {
        this.playerService = playerService;
        this.mapService = mapService;

        // 上线事件
        eventMap.put(EventEnum.ONLINE, (initiator, data) -> {
            return null;
        });

        // 下线事件
        eventMap.put(EventEnum.OFFLINE, (initiator, data) -> {
            // 将玩家坐标信息写入数据库
            playerService.updatePlayerAttribute(initiator, "x", characterAxis.get(initiator).getX());
            playerService.updatePlayerAttribute(initiator, "y", characterAxis.get(initiator).getY());
            // TO-DO: 将玩家宠物的坐标信息写入数据库
            return null;
        });

        // 告知坐标信息
        eventMap.put(EventEnum.COORDINATE, (initiator, data) -> {
            int x = NumUtils.toInt(data.get("x"));
            int y = NumUtils.toInt(data.get("y"));
            String id = data.get("id").toString();
            // 只能控制自己或者是自己的宠物(TO-DO)
            if (!initiator.equals(id)) {
                return null;
            }
            Point position = new Point(x, y);
            // 更新坐标信息
            characterAxis.put(id, position);
            return null;
        });

        // 想要移动
        eventMap.put(EventEnum.MOVE, (initiator, data) -> {
            int x0 = NumUtils.toInt(data.get("x0"));
            int y0 = NumUtils.toInt(data.get("y0"));
            int x1 = NumUtils.toInt(data.get("x1"));
            int y1 = NumUtils.toInt(data.get("y1"));
            // 更新玩家的坐标信息
            characterAxis.put(initiator, new Point(x0, y0));
            // 更新玩家的找到的路径
            // TO-DO: 每种角色的宽度和高度不一样，需要根据角色类型来获取
            List<Point> path = mapService.findPath(x0, y0, x1, y1, (int) (120 * 0.6), (int) (120 * 0.7));
            // 更新玩家的状态
            characterStatus.put(initiator, CharacterStatus.FINDING_PATH);
            // 通知玩家移动
            WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, Map.of(
                    "id", initiator,
                    "speed", playerService.getPlayerInfoByUsername(initiator).getSpeed(),
                    "path", DataCompressor.compressPath(path)
            )));
            return null;
        });

    }

}
