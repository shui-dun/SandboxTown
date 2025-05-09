package com.shuidun.sandbox_town_backend.schedule;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.WSRequestEnum;
import com.shuidun.sandbox_town_backend.service.ItemService;
import com.shuidun.sandbox_town_backend.service.MapService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.utils.Concurrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.Validator;
import java.util.HashMap;
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
                EventDto finalEventDto = eventDto;
                Concurrent.submitTask(() -> consumer.accept(finalEventDto.getInitiator(), finalEventDto.getData()));
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

    public EventHandler(SpriteService spriteService, MapService mapService, ItemService itemService, Validator validator) {
        this.validator = validator;

        // 告知坐标信息
        eventMap.put(WSRequestEnum.COORDINATE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(CoordinateDto.class);
            if (!validate(data)) {
                return;
            }
            // fix: 客户端服务端时间不同步导致精灵坐标回滚的问题
            // 因此暂不进行时间戳校验
            // // 如果时间戳不对，直接返回
            // if (data.getTime() > System.currentTimeMillis() || data.getTime() < System.currentTimeMillis() - 1500) {
            //     return;
            // }
            var sprite = spriteService.selectOnlineById(data.getId());
            // 如果该角色不在线，直接返回
            if (sprite == null) {
                return;
            }

            // TODO: 只能控制自己或者是自己的宠物或者公共npc，如果是其他玩家或者是其他玩家的宠物，直接返回
            // 更新坐标信息
            // 如果传入的时间戳小于上次更新的时间戳，直接返回
            if (sprite.getLastMoveTime() > data.getTime()) {
                return;
            }

            sprite.setX(data.getX());
            sprite.setY(data.getY());
            sprite.setLastMoveTime(data.getTime());
        });

        // 想要移动
        eventMap.put(WSRequestEnum.MOVE, (initiator, mapData) -> {
            var data = mapData.toJavaObject(MoveDto.class);
            if (!validate(data)) {
                return;
            }
            // 如果精灵不在线，返回
            SpriteBo sprite = spriteService.selectOnlineById(initiator);
            if (sprite == null) {
                return;
            }
            sprite.setX(data.getX0());
            sprite.setY(data.getY0());
            if (data.getDestSpriteId() != null) {
                SpriteBo destSprite = spriteService.selectOnlineById(data.getDestSpriteId());
                if (destSprite != null) {
                    sprite.setTargetSpriteId(destSprite.getId());
                }
            } else if (data.getDestBuildingId() != null) {
                sprite.setTargetSpriteId(null);
                sprite.setTargetBuildingId(data.getDestBuildingId());
                sprite.setTargetX(data.getX1());
                sprite.setTargetY(data.getY1());
            } else {
                sprite.setTargetSpriteId(null);
                sprite.setTargetBuildingId(null);
                sprite.setTargetX(data.getX1());
                sprite.setTargetY(data.getY1());
            }
        });

        // 交互事件
        eventMap.put(WSRequestEnum.INTERACT, (initiator, mapData) -> {
            var data = mapData.toJavaObject(InteractDto.class);
            if (!validate(data)) {
                return;
            }
            // 判断上次交互的时间是否过去了400m秒
            var sourceSprite = spriteService.selectOnlineById(data.getSource());
            var targetSprite = spriteService.selectOnlineById(data.getTarget());
            // 如果两者有一个不在线，直接返回
            if (sourceSprite == null || targetSprite == null) {
                return;
            }
            if (sourceSprite.getLastInteractTime() != null && sourceSprite.getLastInteractTime() > System.currentTimeMillis() - 400) {
                return;
            }
            // 如果上次交互的序列号和本次相同，说明本次交互已经处理过了，直接返回
            if (data.getSn().equals(sourceSprite.getLastInteractSn())) {
                return;
            }
            // 如果两者距离较远，直接返回
            if (!mapService.isNear(sourceSprite, targetSprite)) {
                return;
            }
            // 更新交互对象、上次交互的时间和序列号
            sourceSprite.setInteractSpriteId(targetSprite.getId());
            sourceSprite.setLastInteractTime(System.currentTimeMillis());
            sourceSprite.setLastInteractSn(data.getSn());
        });

        // 索敌事件
        eventMap.put(WSRequestEnum.FIND_ENEMY, (initiator, mapData) -> {
            SpriteBo sourceSprite = spriteService.selectOnlineById(initiator);
            // 如果精灵不在线，则返回
            if (sourceSprite == null) {
                return;
            }
            SpriteBo targetSprite = spriteService.getValidTarget(sourceSprite)
                    .map(s -> spriteService.selectOnlineById(s.getId()))
                    .orElse(null);
            // 如果目标不合法，则重新选择目标
            if (targetSprite == null) {
                targetSprite = mapService.findNearestTargetInSight(sourceSprite, (s) -> {
                    // 不能攻击自己的宠物
                    return s.getOwner() == null || !s.getOwner().equals(initiator);
                }).map(s -> spriteService.selectOnlineById(s.getId())).orElse(null);
            }
            // 如果找不到目标，直接返回
            if (targetSprite == null) {
                return;
            }
            // 寻找路径
            sourceSprite.setTargetSpriteId(targetSprite.getId());
        });
    }

}
