package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.TimeFrame;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import org.springframework.stereotype.Service;

@Service
public class TimeService {

    /** 得到当前的时间段以及结束时刻 */
    public TimeFrame getTimeFrame() {
        return GameCache.timeFrame;
    }
}
