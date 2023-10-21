package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.service.StoreService;
import com.shuidun.sandbox_town_backend.service.TimeService;
import com.shuidun.sandbox_town_backend.service.TreeService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimeScheduler {
    private TreeService treeService;

    private StoreService storeService;

    private TimeService timeService;

    private SpriteService spriteService;

    public TimeScheduler(TreeService treeService, StoreService storeService, TimeService timeService, SpriteService spriteService) {
        this.treeService = treeService;
        this.storeService = storeService;
        this.timeService = timeService;
        this.spriteService = spriteService;
    }

    @Scheduled(initialDelay = Constants.DAY_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDay() {
        timeService.enterDay();
        notifyTimeFrame();
        // 刷新苹果数目
        treeService.refreshAll();
        // 刷新商店
        storeService.refreshAll();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DAY);
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
        // 使所有夜行动物（即在晚上出现的动物）都受到烧伤效果
        spriteService.burnAllNightSprites();
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 30000)
    public void notifyTimeFrame() {
        log.info("notifyTimeFrame: {}", timeService.getTimeFrame());
        WSMessageSender.sendResponse(new WSResponseVo(
                WSResponseEnum.TIME_FRAME_NOTIFY,
                timeService.getTimeFrame()
        ));
    }
}