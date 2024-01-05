package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.service.EcosystemService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.service.TimeService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimeScheduler {

    private final TimeService timeService;

    private final SpriteService spriteService;

    private final EcosystemService ecosystemService;

    public TimeScheduler(TimeService timeService, SpriteService spriteService, EcosystemService ecosystemService) {
        this.timeService = timeService;
        this.spriteService = spriteService;
        this.ecosystemService = ecosystemService;
    }

    @Scheduled(initialDelay = Constants.DAY_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDay() {
        timeService.enterDay();
        notifyTimeFrame();
        // 刷新所有建筑
        ecosystemService.refreshAllBuildings();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DAY);
        // 使所有夜行动物（即在晚上出现的动物）都受到烧伤效果
        spriteService.burnAllNightSprites();
    }

    @Scheduled(initialDelay = Constants.DUSK_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDusk() {
        timeService.enterDusk();
        notifyTimeFrame();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DUSK);
    }

    @Scheduled(initialDelay = Constants.NIGHT_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterNight() {
        timeService.enterNight();
        notifyTimeFrame();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.NIGHT);
    }

    @Scheduled(initialDelay = Constants.DAWN_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDawn() {
        timeService.enterDawn();
        notifyTimeFrame();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DAWN);
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 30000)
    public void notifyTimeFrame() {
        log.info("notifyTimeFrame: {}", timeService.getTimeFrame());
        WSMessageSender.addResponse(new WSResponseVo(
                WSResponseEnum.TIME_FRAME_NOTIFY,
                timeService.getTimeFrame()
        ));
    }
}