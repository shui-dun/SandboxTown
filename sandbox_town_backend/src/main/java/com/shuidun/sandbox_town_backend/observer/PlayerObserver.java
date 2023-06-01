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
            log.info("玩家 {} 上线, {}", initiator, players.get(initiator).toString());
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
            // 玩家的速度
            int speed = (int) players.get(initiator).getSpeed();
            // 如果玩家正在寻路
            if (playerStatus.get(initiator) == PlayerStatusEnum.PATHFINDING) {
                // 获得玩家的路径
                Path path = playerPath.get(initiator);
                // 如果玩家已经到达终点
                if (mapService.isNear(path.getPoints().get(path.getPoints().size() - 1), position, speed)) {
                    log.info("玩家到达了终点");
                    // 更新玩家的状态
                    playerStatus.put(initiator, PlayerStatusEnum.NORMAL);
                    // 更新玩家的路径
                    playerPath.remove(initiator);
                    // 通知玩家停止移动
                    WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE,
                            Map.of("x0", x, "y0", y,
                                    "x", x, "y", y, "speed", 0, "id", initiator)));
                    return null;
                }
                // 看看玩家到达了哪里
                int ind = path.getNextPos();
                while (ind < path.getPoints().size() && mapService.isNear(path.getPoints().get(ind), position, speed)) {
                    ind++;
                }
                // 如果玩家偏离了路径
                if (ind == path.getPoints().size()) {
                    log.info("玩家偏离了路径");
                    // 更新玩家的路径（重新进行寻路）
                    playerPath.put(initiator, mapService.findPath(
                            x, y,
                            path.getPoints().get(path.getPoints().size() - 1).getX(),
                            path.getPoints().get(path.getPoints().size() - 1).getY()
                    ));
                } else { // 如果玩家没有偏离路径
                    log.info("玩家按照路径移动");
                    // 更新玩家的路径
                    path.setNextPos(ind);

                }
                // 通知玩家移动
                WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, Map.of(
                        "x0", x, "y0", y,
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
            int x0 = NumUtils.toInt(data.get("x0"));
            int y0 = NumUtils.toInt(data.get("y0"));
            // 更新玩家的坐标信息
            players.get(initiator).setX(x0);
            players.get(initiator).setY(y0);
            // 更新玩家的找到的路径
            Path path = mapService.findPath(
                    x0, y0,
                    NumUtils.toInt(data.get("x1")),
                    NumUtils.toInt(data.get("y1")));
            playerPath.put(initiator, path);
            // 更新玩家的状态
            playerStatus.put(initiator, PlayerStatusEnum.PATHFINDING);
            // 通知玩家移动
            WSManager.sendMessageToAllUsers(new WSResponse(WSResponseEnum.MOVE, Map.of(
                    "x0", x0, "y0", y0,
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
