package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.TimeFrameVo;
import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import org.springframework.stereotype.Service;

@Service
public class TimeService {

    /** 得到当前的时间段以及结束时刻 */
    public TimeFrameVo getTimeFrame() {
        return GameCache.timeFrame;
    }

    /** 进入白天 */
    public void enterDay() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.DAY,
                Constants.DAY_DURATION,
                System.currentTimeMillis() + Constants.DAY_DURATION
        );
    }

    /** 进入黄昏 */
    public void enterDusk() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.DUSK,
                Constants.DUSK_DURATION,
                System.currentTimeMillis() + Constants.DUSK_DURATION
        );
    }

    /** 进入夜晚 */
    public void enterNight() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.NIGHT,
                Constants.NIGHT_DURATION,
                System.currentTimeMillis() + Constants.NIGHT_DURATION
        );
    }

    /** 进入黎明 */
    public void enterDawn() {
        GameCache.timeFrame = new TimeFrameVo(
                TimeFrameEnum.DAWN,
                Constants.DAWN_DURATION,
                System.currentTimeMillis() + Constants.DAWN_DURATION
        );
    }

}
