package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.WSRequestEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 事件处理器
 * 充当中介者的角色
 * 收到事件后，根据事件类型，调用相应的处理函数，里面会调用各个模块的服务
 */
@Slf4j
@Component
public class WSRequestHandler {
    /** 事件类型 -> 处理函数 */
    private final Map<WSRequestEnum, BiFunction<String, JSONObject, Void>> eventMap = new HashMap<>();

    public void handle(EventDto eventDto) {
        try {
            // 如果类型是空，就不处理
            if (eventDto.getType() == null) {
                return;
            }
            eventMap.get(eventDto.getType()).apply(eventDto.getInitiator(), eventDto.getData());
        } catch (Exception e) {
            log.error("handle {} event error", eventDto, e);
        }
    }

    public WSRequestHandler(SpriteService spriteService, GameMapService gameMapService) {


        // 下线事件
        eventMap.put(WSRequestEnum.OFFLINE, (initiator, mapData) -> {
            // 读取角色的所有宠物
            List<SpriteDo> pets = spriteService.selectByOwner(initiator);
            // 删除角色以及其宠物坐标等信息
            GameCache.spriteCacheMap.remove(initiator);
            pets.forEach(pet -> GameCache.spriteCacheMap.remove(pet.getId()));
            // 通知其他玩家
            WSResponseVo wsResponse = new WSResponseVo(WSResponseEnum.OFFLINE, new OfflineVo(initiator));
            WSMessageSender.sendResponse(wsResponse);
            return null;
        });

        // 告知坐标信息
        eventMap.put(WSRequestEnum.COORDINATE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(CoordinateDto.class);
            // 如果是第一次通报坐标信息，说明刚上线
            boolean isFirstTime = !GameCache.spriteCacheMap.containsKey(data.getId());

            // TODO: 只能控制自己或者是自己的宠物或者公共npc
            // 如果是其他玩家或者是其他玩家的宠物，直接返回
            // 更新坐标信息
            var spriteCache = GameCache.spriteCacheMap.get(data.getId());
            if (spriteCache == null) {
                spriteCache = new SpriteCache();
                GameCache.spriteCacheMap.put(data.getId(), spriteCache);
            }
            spriteCache.setX(data.getX());
            spriteCache.setY(data.getY());
            spriteCache.setVx(data.getVx());
            spriteCache.setVy(data.getVy());
            // 广播给其他玩家
            // 如果是第一次通报坐标信息，说明刚上线，需要广播上线信息
            WSResponseVo response;
            if (isFirstTime) {
                // 广播上线信息
                response = new WSResponseVo(WSResponseEnum.ONLINE, spriteService.selectById(data.getId()));

            } else { // 如果不是第一次通报坐标信息，只需广播坐标信息
                response = new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(
                        data.getId(), data.getX(), data.getY(), data.getVx(), data.getVy()
                ));
            }
            WSMessageSender.sendResponse(response);
            return null;
        });

        // 想要移动
        eventMap.put(WSRequestEnum.MOVE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(MoveDto.class);
            // 更新玩家的坐标信息
            var spriteCache = GameCache.spriteCacheMap.get(initiator);
            if (spriteCache == null) {
                spriteCache = new SpriteCache();
                GameCache.spriteCacheMap.put(initiator, spriteCache);
            }
            spriteCache.setX(data.getX0());
            spriteCache.setY(data.getY0());
            spriteCache.setVx(0);
            spriteCache.setVy(0);
            // 更新玩家的找到的路径
            var sprite = spriteService.selectByIdWithDetail(initiator);
            // 每种角色的宽度和高度不一样，需要根据角色类型来获取相应路径
            List<Point> path = gameMapService.findPath(
                    sprite, data.getX1(), data.getY1(),
                    data.getDestBuildingId(), data.getDestSpriteId());
            // 如果找不到路径，直接返回
            if (path == null) {
                return null;
            }
            // TODO: 更新玩家的状态
            // 通知玩家移动
            WSMessageSender.sendResponse(new WSResponseVo(WSResponseEnum.MOVE, new MoveVo(
                    initiator,
                    sprite.getSpeed() + sprite.getSpeedInc(),
                    DataCompressor.compressPath(path),
                    data.getDestBuildingId(),
                    data.getDestSpriteId()
            )));
            return null;
        });

        // 交互事件
        eventMap.put(WSRequestEnum.INTERACT, (initiator, mapData) -> {
            var data = mapData.toJavaObject(InteractDto.class);
            var sourceSprite = spriteService.selectByIdWithDetail(data.getSource());
            var targetSprite = spriteService.selectByIdWithDetail(data.getTarget());
            // 目前只添加了攻击事件
            List<WSResponseVo> responses = spriteService.attack(sourceSprite, targetSprite);
            WSMessageSender.sendResponseList(responses);
            return null;
        });

    }

}
