package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeFrameVo {
    private TimeFrameEnum timeFrame;

    /** 当前时间段的总持续时间（单位ms） */
    private long timeFrameDuration;

    /** 当前时间段的结束时刻（1970年至今的毫秒数） */
    private long timeFrameEndTime;
}
