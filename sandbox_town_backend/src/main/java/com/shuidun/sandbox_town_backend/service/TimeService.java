package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.TimeFrameVo;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TimeService {
    private final SpriteService spriteService;

    private final EcosystemService ecosystemService;

    public TimeService(SpriteService spriteService, EcosystemService ecosystemService) {
        this.spriteService = spriteService;
        this.ecosystemService = ecosystemService;
    }

    public void enterDay() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.DAY,
                Constants.DAY_DURATION,
                System.currentTimeMillis() + Constants.DAY_DURATION
        );
        notifyTimeFrame();
        // 刷新所有建筑
        ecosystemService.refreshAllBuildings();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DAY);
        // 使所有夜行动物（即在晚上出现的动物）都受到烧伤效果
        spriteService.burnAllNightSprites();
    }

    public void enterDusk() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.DUSK,
                Constants.DUSK_DURATION,
                System.currentTimeMillis() + Constants.DUSK_DURATION
        );
        notifyTimeFrame();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DUSK);
    }

    public void enterNight() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.NIGHT,
                Constants.NIGHT_DURATION,
                System.currentTimeMillis() + Constants.NIGHT_DURATION
        );
        notifyTimeFrame();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.NIGHT);
    }

    public void enterDawn() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.DAWN,
                Constants.DAWN_DURATION,
                System.currentTimeMillis() + Constants.DAWN_DURATION
        );
        notifyTimeFrame();
        // 刷新动物
        spriteService.refreshSprites(TimeFrameEnum.DAWN);
    }

    public void notifyTimeFrame() {
        log.info("notifyTimeFrame: {}", getTimeFrame());
        WSMessageSender.addResponse(new WSResponseVo(
                WSResponseEnum.TIME_FRAME_NOTIFY,
                getTimeFrame()
        ));
    }

    /** 得到当前的时间段以及结束时刻 */
    public TimeFrameVo getTimeFrame() {
        return GameCache.timeFrame;
    }
}
