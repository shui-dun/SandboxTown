package com.shuidun.sandbox_town_backend.controller;

import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.bean.TimeFrameVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.TimeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/time")
public class TimeController {

    public TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @Operation(summary = "得到当前的时间段以及结束时刻")
    @GetMapping("/getTimeFrame")
    public RestResponseVo<TimeFrameVo> getTimeFrame() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, timeService.getTimeFrame());
    }
}
