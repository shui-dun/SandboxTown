package com.shuidun.sandbox_town_backend.schedule;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.agent.UserAgent;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.FeedResultEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSRequestEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.ItemService;
import com.shuidun.sandbox_town_backend.service.SpriteActionService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

/**
 * 事件处理器
 * 充当中介者的角色
 * 收到事件后，根据事件类型，调用相应的处理函数，里面会调用各个模块的服务
 */
@Slf4j
@Component
public class EventHandler {
    /** 事件类型 -> 处理函数 */
    private final Map<WSRequestEnum, BiConsumer<String, JSONObject>> eventMap = new HashMap<>();

    /**
     * 消息队列
     * 避免多线程并发问题
     * 之前是打算通过加锁解决，但是导致死锁、性能下降和编程复杂
     */
    private final LinkedBlockingQueue<EventDto> mq = new LinkedBlockingQueue<>();

    private final Validator validator;

    /** 向消息队列中添加消息 */
    public void addMessage(EventDto eventDto) {
        mq.add(eventDto);
    }

    /** 处理消息 */
    public void handleMessages() {
        while (true) {
            EventDto eventDto = null;
            try {
                // 从队列的头部检索并删除元素，如果队列为空，则返回null
                eventDto = mq.poll();
                if (eventDto == null) {
                    break;
                }
                // 如果类型是空，就不处理
                if (eventDto.getType() == null) {
                    continue;
                }
                var consumer = eventMap.get(eventDto.getType());
                assert consumer != null;
                consumer.accept(eventDto.getInitiator(), eventDto.getData());
            } catch (Exception e) {
                log.error("handle {} event error", eventDto, e);
            }
        }
    }

    /** 进行校验 */
    private <T> boolean validate(T data) {
        var violations = validator.validate(data);
        if (!violations.isEmpty()) {
            log.error("validate error: {}, data: {}", violations, data);
            return false;
        }
        return true;
    }

    public EventHandler(SpriteService spriteService, GameMapService gameMapService, SpriteActionService spriteActionService, ItemService itemService, Validator validator, UserAgent userAgent) {
        this.validator = validator;

        // 告知坐标信息
        eventMap.put(WSRequestEnum.COORDINATE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(CoordinateDto.class);
            if (!validate(data)) {
                return;
            }
            // 如果时间戳不对，直接返回
            if (data.getTime() > System.currentTimeMillis() || data.getTime() < System.currentTimeMillis() - 1500) {
                return;
            }
            var sprite = spriteService.selectById(initiator);
            assert sprite != null;
            var spriteCache = sprite.getOnlineCache();
            // 如果该角色已被删除，直接返回
            if (spriteCache == null
                    && spriteService.selectById(data.getId()) == null) {
                return;
            }

            // TODO: 只能控制自己或者是自己的宠物或者公共npc，如果是其他玩家或者是其他玩家的宠物，直接返回
            // 更新坐标信息
            // 如果传入的时间戳小于上次更新的时间戳，直接返回
            if (spriteCache != null && spriteCache.getLastMoveTime() > data.getTime()) {
                return;
            }

            if (spriteCache == null) {
                spriteCache = spriteService.online(data.getId());
            }
            spriteCache.setX(data.getX());
            spriteCache.setY(data.getY());
            spriteCache.setLastMoveTime(data.getTime());
            spriteCache.setVx(data.getVx());
            spriteCache.setVy(data.getVy());

            // 广播给其他玩家
            WSMessageSender.addResponse(
                    new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(
                            data.getId(), data.getX(), data.getY(), data.getVx(), data.getVy()
                    )));
        });

        // 想要移动
        eventMap.put(WSRequestEnum.MOVE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(MoveDto.class);
            if (!validate(data)) {
                return;
            }
            // 更新玩家的坐标信息
            SpriteDetailBo sprite = spriteService.selectById(initiator);
            // 如果精灵不存在
            if (sprite == null) {
                return;
            }
            // 如果精灵不在线，使其在线
            if (sprite.getOnlineCache() == null) {
                sprite.setOnlineCache(spriteService.online(initiator));
            }
            sprite.setX(data.getX0());
            sprite.setY(data.getY0());
            sprite.getOnlineCache().setX(data.getX0());
            sprite.getOnlineCache().setY(data.getY0());
            sprite.getOnlineCache().setVx(0.0);
            sprite.getOnlineCache().setVy(0.0);
            // 寻找路径
            MoveBo moveBo = MoveBo.empty();
            if (data.getDestSpriteId() != null) {
                SpriteWithTypeBo destSprite = spriteService.selectById(data.getDestSpriteId());
                if (destSprite != null) {
                    moveBo = MoveBo.moveToSprite(destSprite, data.getX1(), data.getY1());
                }
            } else if (data.getDestBuildingId() != null) {
                moveBo = MoveBo.moveToBuilding(data.getDestBuildingId(), data.getX1(), data.getY1());
            } else {
                moveBo = MoveBo.moveToPoint(data.getX1(), data.getY1());
            }
            MoveVo moveVo = spriteActionService.move(sprite, moveBo, userAgent.mapBitsPermissions(sprite));
            if (moveVo != null) {
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, moveVo));
            }
        });

        // 交互事件
        eventMap.put(WSRequestEnum.INTERACT, (initiator, mapData) -> {
            var data = mapData.toJavaObject(InteractDto.class);
            if (!validate(data)) {
                return;
            }
            // 判断上次交互的时间是否过去了400m秒
            var sourceSprite = spriteService.selectById(data.getSource());
            var targetSprite = spriteService.selectById(data.getTarget());
            // 如果两者有一个不存在，直接返回
            if (sourceSprite == null || targetSprite == null) {
                return;
            }
            var spriteCache = sourceSprite.getOnlineCache();
            if (spriteCache == null || (spriteCache.getLastInteractTime() != null && spriteCache.getLastInteractTime() > System.currentTimeMillis() - 400)) {
                return;
            }
            // 如果上次交互的序列号和本次相同，说明本次交互已经处理过了，直接返回
            if (data.getSn().equals(spriteCache.getLastInteractSn())) {
                return;
            }
            // 如果两者距离较远，直接返回
            if (!spriteActionService.isNear(sourceSprite, targetSprite)) {
                return;
            }
            // 更新上次交互的时间和序列号
            spriteCache.setLastInteractTime(System.currentTimeMillis());
            spriteCache.setLastInteractSn(data.getSn());
            // 先尝试驯服/喂养
            FeedResultEnum feedResult = spriteService.feed(sourceSprite, targetSprite);
            // 如果驯服结果是“已经有主人”或者“驯服成功”或者“驯服失败”或者“喂养成功”，说明本次交互的目的的确是驯服/喂养，而非攻击
            if (feedResult == FeedResultEnum.ALREADY_TAMED || feedResult == FeedResultEnum.TAME_SUCCESS
                    || feedResult == FeedResultEnum.TAME_FAIL || feedResult == FeedResultEnum.FEED_SUCCESS) {
                // 发送驯服/喂养结果通知
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.FEED_RESULT, new FeedVo(
                        sourceSprite.getId(), targetSprite.getId(), feedResult
                )));
                // 驯服会消耗物品，因此发送通知栏变化通知
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY,
                        new ItemBarNotifyVo(sourceSprite.getId())));
                return;
            }
            // 否则本次交互的目的是进行攻击
            List<WSResponseVo> responses = spriteService.attack(sourceSprite, targetSprite);
            WSMessageSender.addResponses(responses);
        });

        // 索敌事件
        eventMap.put(WSRequestEnum.FIND_ENEMY, (initiator, mapData) -> {
            SpriteDetailBo sourceSprite = spriteService.selectById(initiator);
            // 如果精灵不存在或者不在线，则返回
            if (sourceSprite == null || sourceSprite.getOnlineCache() == null) {
                return;
            }
            SpriteWithTypeBo targetSprite = spriteActionService.getValidTarget(sourceSprite)
                    .map(s -> spriteService.selectById(s.getId()))
                    .orElse(null);
            // 如果目标不合法，则重新选择目标
            if (targetSprite == null) {
                targetSprite = spriteActionService.findNearestTargetInSight(sourceSprite, (s) -> {
                    // 不能攻击自己的宠物
                    return s.getOwner() == null || !s.getOwner().equals(initiator);
                }).map(s -> spriteService.selectById(s.getId())).orElse(null);
            }
            // 如果找不到目标，直接返回
            if (targetSprite == null) {
                return;
            }
            // 寻找路径
            MoveVo moveVo = spriteActionService.move(sourceSprite, MoveBo.moveToSprite(targetSprite), userAgent.mapBitsPermissions(sourceSprite));
            if (moveVo != null) {
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, moveVo));
            }
        });
    }

}
