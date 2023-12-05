package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.TreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Validated
@Slf4j
@RestController
@RequestMapping("/tree")
public class TreeController {

    private final TreeService treeService;

    public TreeController(TreeService treeService) {
        this.treeService = treeService;
    }

    @Operation(summary = "摘苹果")
    @PostMapping("/pickApple")
    public RestResponseVo<Void> pickApple(@NotNull @RequestParam String treeId) {
        treeService.pickApple(StpUtil.getLoginIdAsString(), treeId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "检查是否可以摘苹果，返回的状态码为success表示可以摘苹果，否则不可以")
    @GetMapping("/canPickApple")
    public RestResponseVo<Void> canPickApple(@NotNull @RequestParam @Parameter(description = "树的id")
                                             String treeId) {
        treeService.checkPickApple(StpUtil.getLoginIdAsString(), treeId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
