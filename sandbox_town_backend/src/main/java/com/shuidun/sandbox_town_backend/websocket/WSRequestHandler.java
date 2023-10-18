package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.ItemService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 事件处理器
 * 充当中介者的角色
 * 收到事件后，根据事件类型，调用相应的处理函数，里面会调用各个模块的服务
 */
@Slf4j
@Component
public class WSRequestHandler {
    /** 事件类型 -> 处理函数 */
    private final Map<WSRequestEnum, BiConsumer<String, JSONObject>> eventMap = new HashMap<>();

    public void handle(EventDto eventDto) {
        try {
            // 如果类型是空，就不处理
            if (eventDto.getType() == null) {
                return;
            }
            eventMap.get(eventDto.getType()).accept(eventDto.getInitiator(), eventDto.getData());
        } catch (Exception e) {
            log.error("handle {} event error", eventDto, e);
        }
    }

    public WSRequestHandler(SpriteService spriteService, GameMapService gameMapService, ItemService itemService) {


        // 告知坐标信息
        eventMap.put(WSRequestEnum.COORDINATE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(CoordinateDto.class);
            // 如果时间戳不对，直接返回
            if (data.getTime() == null || data.getTime() > System.currentTimeMillis() || data.getTime() < System.currentTimeMillis() - 1500) {
                return;
            }
            // 如果该角色已被删除，直接返回
            if (!GameCache.spriteCacheMap.containsKey(data.getId())
                    && spriteService.selectById(data.getId()) == null) {
                return;
            }

            // TODO: 只能控制自己或者是自己的宠物或者公共npc，如果是其他玩家或者是其他玩家的宠物，直接返回
            // 更新坐标信息
            var spriteCache = GameCache.spriteCacheMap.get(data.getId());
            // 如果传入的时间戳小于上次更新的时间戳，直接返回
            if (spriteCache != null && spriteCache.getLastMoveTime() > data.getTime()) {
                return;
            }

            if (spriteCache == null) {
                spriteCache = new SpriteCache();
                GameCache.spriteCacheMap.put(data.getId(), spriteCache);
            }
            spriteCache.setX(data.getX());
            spriteCache.setY(data.getY());
            spriteCache.setLastMoveTime(data.getTime());
            spriteCache.setVx(data.getVx());
            spriteCache.setVy(data.getVy());

            // 广播给其他玩家
            WSMessageSender.sendResponse(
                    new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(
                            data.getId(), data.getX(), data.getY(), data.getVx(), data.getVy()
                    )));
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
                return;
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
        });

        // 交互事件
        eventMap.put(WSRequestEnum.INTERACT, (initiator, mapData) -> {
            var data = mapData.toJavaObject(InteractDto.class);
            // 判断上次交互的时间是否过去了300m秒
            var spriteCache = GameCache.spriteCacheMap.get(data.getSource());
            if (spriteCache == null || spriteCache.getLastInteractTime() > System.currentTimeMillis() - 300) {
                return;
            }
            var sourceSprite = spriteService.selectByIdWithDetail(data.getSource());
            var targetSprite = spriteService.selectByIdWithDetail(data.getTarget());
            // 如果两者距离较远，直接返回
            if (!spriteService.isNear(sourceSprite, targetSprite)) {
                return;
            }
            spriteCache.setLastInteractTime(System.currentTimeMillis());
            // 先尝试驯服
            TameResultEnum tameResult = spriteService.tame(sourceSprite, targetSprite);
            // 如果驯服结果是“已经有主人”或者“驯服成功”或者“驯服失败”，说明本次交互的目的的确是驯服，而非攻击
            if (tameResult == TameResultEnum.ALREADY_TAMED || tameResult == TameResultEnum.SUCCESS || tameResult == TameResultEnum.FAIL) {
                // 发送驯服结果通知
                WSMessageSender.sendResponse(new WSResponseVo(WSResponseEnum.TAME_RESULT, new TameVo(
                        sourceSprite.getId(), targetSprite.getId(), tameResult
                )));
                // 如果驯服结果不是“已经有主人”，则代表的确尝试去驯服，会消耗物品，因此发送通知栏变化通知
                if (tameResult != TameResultEnum.ALREADY_TAMED) {
                    WSMessageSender.sendResponse(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY,
                            new ItemBarNotifyVo(sourceSprite.getId())));
                }
                return;
            }
            // 否则本次交互的目的是进行攻击
            List<WSResponseVo> responses = spriteService.attack(sourceSprite, targetSprite);
            WSMessageSender.sendResponseList(responses);
        });

    }

}
