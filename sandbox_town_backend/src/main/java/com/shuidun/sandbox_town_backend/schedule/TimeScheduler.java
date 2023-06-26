package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.service.StoreService;
import com.shuidun.sandbox_town_backend.service.TreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimeScheduler {
    // 白天时长5分钟（300000ms），黄昏时长1分钟（60000ms），夜晚时长3分钟（180000ms），黎明时长1分钟（60000ms）
    // 因此，白天从0ms开始，黄昏从300000ms开始，夜晚从360000ms开始，黎明从540000ms开始
    // 一天总时长为10分钟（600000ms）

    private TreeService treeService;

    private StoreService storeService;

    public TimeScheduler(TreeService treeService, StoreService storeService) {
        this.treeService = treeService;
        this.storeService = storeService;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 600000)
    public void enterMorning() {
        log.info("morning");
        // 刷新苹果数目
        treeService.refreshTrees();
        // 刷新商店
        storeService.refreshAll();
    }

    @Scheduled(initialDelay = 300000, fixedDelay = 600000)
    public void enterDusk() {
        log.info("dusk");
    }

    @Scheduled(initialDelay = 360000, fixedDelay = 600000)
    public void enterNight() {
        log.info("night");
    }

    @Scheduled(initialDelay = 540000, fixedDelay = 600000)
    public void enterDawn() {
        log.info("dawn");
    }
}