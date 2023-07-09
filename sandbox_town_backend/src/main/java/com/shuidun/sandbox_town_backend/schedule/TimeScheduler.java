package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.service.StoreService;
import com.shuidun.sandbox_town_backend.service.TreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimeScheduler {

    // 因此，白天从0ms开始，黄昏从300000ms开始，夜晚从360000ms开始，黎明从540000ms开始


    private TreeService treeService;

    private StoreService storeService;

    public TimeScheduler(TreeService treeService, StoreService storeService) {
        this.treeService = treeService;
        this.storeService = storeService;
    }

    @Scheduled(initialDelay = Constants.DAY_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDay() {
        log.info("enter day");
        GameCache.timeFrame.setTimeFrame(TimeFrameEnum.DAY);
        GameCache.timeFrame.setTimeFrameDuration(Constants.DAY_DURATION);
        GameCache.timeFrame.setTimeFrameEndTime(System.currentTimeMillis() + Constants.DAY_DURATION);
        // 刷新苹果数目
        treeService.refreshTrees();
        // 刷新商店
        storeService.refreshAll();
    }

    @Scheduled(initialDelay = Constants.DUSK_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDusk() {
        log.info("enter dusk");
        GameCache.timeFrame.setTimeFrame(TimeFrameEnum.DUSK);
        GameCache.timeFrame.setTimeFrameDuration(Constants.DUSK_DURATION);
        GameCache.timeFrame.setTimeFrameEndTime(System.currentTimeMillis() + Constants.DUSK_DURATION);

    }

    @Scheduled(initialDelay = Constants.NIGHT_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterNight() {
        log.info("enter night");
        GameCache.timeFrame.setTimeFrame(TimeFrameEnum.NIGHT);
        GameCache.timeFrame.setTimeFrameDuration(Constants.NIGHT_DURATION);
        GameCache.timeFrame.setTimeFrameEndTime(System.currentTimeMillis() + Constants.NIGHT_DURATION);
    }

    @Scheduled(initialDelay = Constants.DAWN_START, fixedDelay = Constants.DAY_TOTAL_DURATION)
    public void enterDawn() {
        log.info("enter dawn");
        GameCache.timeFrame.setTimeFrame(TimeFrameEnum.DAWN);
        GameCache.timeFrame.setTimeFrameDuration(Constants.DAWN_DURATION);
        GameCache.timeFrame.setTimeFrameEndTime(System.currentTimeMillis() + Constants.DAWN_DURATION);
    }
}