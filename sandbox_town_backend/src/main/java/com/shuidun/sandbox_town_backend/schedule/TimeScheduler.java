package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.service.StoreService;
import com.shuidun.sandbox_town_backend.service.TimeService;
import com.shuidun.sandbox_town_backend.service.TreeService;
import com.shuidun.sandbox_town_backend.websocket.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimeScheduler {
    private TreeService treeService;

    private StoreService storeService;

    private TimeService timeService;

    public TimeScheduler(TreeService treeService, StoreService storeService, TimeService timeService) {
        this.treeService = treeService;
        this.storeService = storeService;
        this.timeService = timeService;
    }

    @Scheduled(initialDelay = Constants.DAY_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDay() {
        timeService.enterDay();
        notifyTimeFrame();
        // 刷新苹果数目
        treeService.refreshTrees();
        // 刷新商店
        storeService.refreshAll();
    }

    @Scheduled(initialDelay = Constants.DUSK_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDusk() {
        timeService.enterDusk();
        notifyTimeFrame();
    }

    @Scheduled(initialDelay = Constants.NIGHT_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterNight() {
        timeService.enterNight();
        notifyTimeFrame();
    }

    @Scheduled(initialDelay = Constants.DAWN_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDawn() {
        timeService.enterDawn();
        notifyTimeFrame();
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 30000)
    public void notifyTimeFrame() {
        log.info("notifyTimeFrame: {}", timeService.getTimeFrame());
        MessageSender.sendMessageToAllUsers(new WSResponseVo(
                WSResponseEnum.TIME_FRAME_NOTIFY,
                timeService.getTimeFrame()
        ));
    }
}