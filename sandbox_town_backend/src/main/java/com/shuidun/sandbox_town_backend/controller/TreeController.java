package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.TreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tree")
public class TreeController {

    private final TreeService treeService;

    public TreeController(TreeService treeService) {
        this.treeService = treeService;
    }

    /** 摘苹果 */
    @PostMapping("/pickApple")
    public RestResponseVo<?> pickApple(String treeId) {
        treeService.pickApple(StpUtil.getLoginIdAsString(), treeId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    /**
     * 检查是否可以摘苹果
     *
     * @param treeId 树的id
     * @return 状态码为success表示可以摘苹果，否则不可以
     */
    @GetMapping("/canPickApple")
    public RestResponseVo<?> canPickApple(String treeId) {
        treeService.checkPickApple(StpUtil.getLoginIdAsString(), treeId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }
}
