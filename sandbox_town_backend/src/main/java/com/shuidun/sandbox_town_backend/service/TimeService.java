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
        TimeFrameVo timeFrameVo = new TimeFrameVo();
        timeFrameVo.setTimeFrame(TimeFrameEnum.DAY);
        timeFrameVo.setTimeFrameDuration(Constants.DAY_DURATION);
        timeFrameVo.setTimeFrameEndTime(System.currentTimeMillis() + Constants.DAY_DURATION);
        GameCache.timeFrame = timeFrameVo;
    }

    /** 进入黄昏 */
    public void enterDusk() {
        TimeFrameVo timeFrameVo = new TimeFrameVo();
        timeFrameVo.setTimeFrame(TimeFrameEnum.DUSK);
        timeFrameVo.setTimeFrameDuration(Constants.DUSK_DURATION);
        timeFrameVo.setTimeFrameEndTime(System.currentTimeMillis() + Constants.DUSK_DURATION);
        GameCache.timeFrame = timeFrameVo;
    }

    /** 进入夜晚 */
    public void enterNight() {
        TimeFrameVo timeFrameVo = new TimeFrameVo();
        timeFrameVo.setTimeFrame(TimeFrameEnum.NIGHT);
        timeFrameVo.setTimeFrameDuration(Constants.NIGHT_DURATION);
        timeFrameVo.setTimeFrameEndTime(System.currentTimeMillis() + Constants.NIGHT_DURATION);
        GameCache.timeFrame = timeFrameVo;
    }

    /** 进入黎明 */
    public void enterDawn() {
        TimeFrameVo timeFrameVo = new TimeFrameVo();
        timeFrameVo.setTimeFrame(TimeFrameEnum.DAWN);
        timeFrameVo.setTimeFrameDuration(Constants.DAWN_DURATION);
        timeFrameVo.setTimeFrameEndTime(System.currentTimeMillis() + Constants.DAWN_DURATION);
        GameCache.timeFrame = timeFrameVo;
    }

}
