package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.FusionRequestDto;
import com.shuidun.sandbox_town_backend.bean.FusionResultVo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.FusionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/fusion")
@Slf4j
public class FusionController {
    private final FusionService fusionService;

    public FusionController(FusionService fusionService) {
        this.fusionService = fusionService;
    }

    @Operation(summary = "Check fusion result without executing it")
    @PostMapping("/check")
    public RestResponseVo<FusionResultVo> checkFusion(@RequestBody FusionRequestDto request) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
            fusionService.checkFusion(StpUtil.getLoginIdAsString(), request));
    }

    @Operation(summary = "Execute fusion with given items")
    @PostMapping("/execute")
    public RestResponseVo<Void> executeFusion(@RequestBody FusionRequestDto request) {
        fusionService.executeFusion(StpUtil.getLoginIdAsString(), request);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}