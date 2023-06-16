package com.shuidun.sandbox_town_backend.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpriteScheduler {



    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void dog() {

    }
}
