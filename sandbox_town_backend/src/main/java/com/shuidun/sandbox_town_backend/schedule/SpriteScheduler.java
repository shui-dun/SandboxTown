package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.agent.SpriteAgent;
import com.shuidun.sandbox_town_backend.bean.MoveBo;
import com.shuidun.sandbox_town_backend.bean.MoveVo;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.service.GameMapService;
import com.shuidun.sandbox_town_backend.service.SpriteActionService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SpriteScheduler {

    private final SpriteService spriteService;

    private final GameMapService gameMapService;

    private final SpriteActionService spriteActionService;

    public SpriteScheduler(SpriteService spriteService, List<SpriteAgent> spriteAgents, GameMapService gameMapService, SpriteActionService spriteActionService) {
        this.spriteService = spriteService;
        this.gameMapService = gameMapService;
        this.spriteActionService = spriteActionService;
        for (SpriteAgent agent : spriteAgents) {
            typeToAgent.put(agent.getType(), agent);
        }
    }

    /** 精灵类型到精灵Agent的映射 */
    private final Map<SpriteTypeEnum, SpriteAgent> typeToAgent = new HashMap<>();

    private long counterOfSchedule = 0;

    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void schedule() {
        counterOfSchedule++;
        // 遍历所有角色
        for (String id : spriteService.getOnlineSpritesCache().keySet()) {
            // 得到其角色
            SpriteDetailBo sprite = spriteService.selectByIdWithDetail(id);
            // 如果精灵不存在或者不在线，就不处理
            if (sprite == null || sprite.getCache() == null) {
                continue;
            }
            // 生命效果
            if (counterOfSchedule % 12 == 0) {
                if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.LIFE))) {
                    WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), 1));
                }
            }
            // 烧伤效果
            if (counterOfSchedule % 2 == 0) {
                if (sprite.getEffects().stream().anyMatch(x -> x.getEffect().equals(EffectEnum.BURN))) {
                    WSMessageSender.addResponses(spriteService.modifyLife(sprite.getId(), -1));
                }
            }
            // 调用对应的处理函数
            var agent = typeToAgent.get(sprite.getType());
            if (agent != null) {
                MoveBo moveBo = agent.act(sprite);
                MoveVo moveVo = spriteActionService.move(sprite, moveBo, agent.mapBitsPermissions(sprite));
                if (moveVo == null) {
                    continue;
                }
                WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.MOVE, moveVo));
            }
            // 保存坐标
            spriteService.updatePosition(sprite.getId(), sprite.getX(), sprite.getY());
        }

        // 其实当计数器重置时，会导致所有这些定时任务的执行时间都会不准确
        // 但是这个问题不大，因为Long.MAX_VALUE是一个很大的数，在有限的时间内不会重置
        if (counterOfSchedule == Long.MAX_VALUE) {
            counterOfSchedule = 0;
        }
    }

    private long counterOfBatchSchedule = 0;

    @Scheduled(initialDelay = 500, fixedDelay = 1000)
    public void batchSchedule() {
        counterOfBatchSchedule++;
        // 减少饱腹值
        if (counterOfBatchSchedule % 20 == 0) {
            spriteService.reduceSpritesHunger(spriteService.getOnlineSpritesCache().keySet(), 1);
        }
        // 恢复体力
        if (counterOfBatchSchedule % 13 == 0) {
            spriteService.recoverSpritesLife(spriteService.getOnlineSpritesCache().keySet(), Constants.HUNGER_THRESHOLD, 1);
        }

        if (counterOfBatchSchedule == Long.MAX_VALUE) {
            counterOfBatchSchedule = 0;
        }
    }
}
