package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.TimeFrameEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeFrameVo {
    private TimeFrameEnum timeFrame;

    @Schema(description = "当前时间段的总持续时间（单位ms）")
    private Long timeFrameDuration;

    @Schema(description = "当前时间段的结束时刻（1970年至今的毫秒数）")
    private Long timeFrameEndTime;
}
