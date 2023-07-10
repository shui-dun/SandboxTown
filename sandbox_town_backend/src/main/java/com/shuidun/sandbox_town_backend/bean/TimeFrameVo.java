package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeFrameVo {
    private TimeFrameEnum timeFrame;

    /** 当前时间段的持续时间 */
    private long timeFrameDuration;

    /** 当前时间段的结束时刻 */
    private long timeFrameEndTime;
}
