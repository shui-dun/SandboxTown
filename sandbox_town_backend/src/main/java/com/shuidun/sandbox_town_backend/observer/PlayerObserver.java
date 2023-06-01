package com.shuidun.sandbox_town_backend.observer;

import com.shuidun.sandbox_town_backend.bean.Path;
import com.shuidun.sandbox_town_backend.bean.Player;
import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.service.MapService;
import com.shuidun.sandbox_town_backend.service.PlayerService;
import com.shuidun.sandbox_town_backend.utils.NumUtils;
import com.shuidun.sandbox_town_backend.websocket.EventWebSocketHandler;
import com.shuidun.sandbox_town_backend.websocket.WSManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PlayerObserver extends AbstractObserver {
    // 玩家状态枚举类
    private enum PlayerStatusEnum {
        // 正常状态
        NORMAL,
        // 寻路状态
        PATHFINDING,
    }

    // 玩家的信息
    private Map<String, Player> players = new ConcurrentHashMap<>();

    // 玩家的状态信息
    private Map<String, PlayerStatusEnum> playerStatus = new ConcurrentHashMap<>();

    // 寻路
    private Map<String, Path> playerPath = new ConcurrentHashMap<>();

    private final PlayerService playerService;

    private final MapService mapService;

    public PlayerObserver(PlayerService playerService, MapService mapService) {
        this.playerService = playerService;
        this.mapService = mapService;

        // 上线事件
        mp.put(EventEnum.ONLINE, (initiator, data) -> {
            // 从数据库中读取玩家的信息
            players.put(initiator, playerService.getPlayerInfoByUsername(initiator));
            playerStatus.put(initiator, PlayerStatusEnum.NORMAL);
            return null;
        });
        ObserverNotifier.register(EventEnum.ONLINE, this);

        // 下线事件
        mp.put(EventEnum.OFFLINE, (initiator, data) -> {
            // 将玩家的信息写入数据库
            playerService.normalizeAndUpdatePlayer(players.get(initiator));
            players.remove(initiator);
            return null;
        });
        ObserverNotifier.register(EventEnum.OFFLINE, this);

        // 告知坐标信息
        mp.put(EventEnum.COORDINATE, (initiator, data) -> {
            int x = NumUtils.toInt(data.get("x"));
            int y = NumUtils.toInt(data.get("y"));
            Point position = new Point(x, y);
            // 更新玩家的坐标信息
            players.get(initiator).setX(x);
            players.get(initiator).setY(y);
            // 如果玩家正在寻路
            if (playerStatus.get(initiator) == PlayerStatusEnum.PATHFINDING) {
                // 获得玩家的路径
                Path path = playerPath.get(initiator);
                // 如果玩家已经到达终点
                if (mapService.isNear(path.getPoints().get(path.getPoints().size() - 1), position)) {
                    // 更新玩家的状态
                    playerStatus.put(initiator, PlayerStatusEnum.NORMAL);
                    // 更新玩家的路径
                    // playerPath.put(initiator, null);
                    // 通知玩家停止移动
                    WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE,
                            Map.of("x", x, "y", y, "speed", 0, "id", initiator)));
                    return null;
                }
                // 如果玩家的坐标已经到达了下一个点
                if (mapService.isNear(path.getPoints().get(path.getNextPos()), position)) {
                    // 更新玩家的路径
                    path.setNextPos(path.getNextPos() + 1);
                } else if (mapService.isFar(path.getPoints().get(path.getNextPos()), position)) { // 如果玩家偏离了路径
                    // 更新玩家的路径（重新进行寻路）
                    playerPath.put(initiator, mapService.findPath(
                            (int) data.get("x0"), (int) data.get("y0"),
                            (int) data.get("x1"), (int) data.get("y1")));
                }
                // 通知玩家移动
                WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, Map.of(
                        "x", path.getPoints().get(path.getNextPos()).getX(),
                        "y", path.getPoints().get(path.getNextPos()).getY(),
                        "speed", players.get(initiator).getSpeed(),
                        "id", initiator
                )));
            }
            return null;
        });
        ObserverNotifier.register(EventEnum.COORDINATE, this);

        // 想要移动
        mp.put(EventEnum.MOVE, (initiator, data) -> {
            // 更新玩家的找到的路径
            Path path = mapService.findPath(
                    NumUtils.toInt(data.get("x0")),
                    NumUtils.toInt(data.get("y0")),
                    NumUtils.toInt(data.get("x1")),
                    NumUtils.toInt(data.get("y1")));
            playerPath.put(initiator, path);
            // 更新玩家的状态
            playerStatus.put(initiator, PlayerStatusEnum.PATHFINDING);
            log.info("找到的路径： {}", playerPath.get(initiator));
            // 通知玩家移动
            WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, Map.of(
                    "x", path.getPoints().get(path.getNextPos()).getX(),
                    "y", path.getPoints().get(path.getNextPos()).getY(),
                    "speed", players.get(initiator).getSpeed(),
                    "id", initiator
            )));
            return null;
        });
        ObserverNotifier.register(EventEnum.MOVE, this);
    }
}
