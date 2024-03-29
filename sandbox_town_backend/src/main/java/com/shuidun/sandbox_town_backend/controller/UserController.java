package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@Validated
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public RestResponseVo<Void> login(@NotNull @RequestParam String username,
                                      @NotNull @RequestParam String password,
                                      @NotNull @RequestParam boolean rememberMe) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        userService.login(username, password);
        StpUtil.login(username, rememberMe);
        log.info("{} login success, rememberMe: {}", username, rememberMe);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/signup")
    public RestResponseVo<Void> signup(@NotNull @RequestParam String usernameSuffix,
                                       @NotNull @RequestParam String password) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        String username = userService.signup(usernameSuffix, password);
        // 打印注册信息
        log.info("{} signup success", username);
        StpUtil.login(username);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/logout")
    public RestResponseVo<Void> logout() {
        StpUtil.logout();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @GetMapping("/getUsername")
    public RestResponseVo<String> getUsername() {
        if (StpUtil.isLogin()) {
            return new RestResponseVo<>(StatusCodeEnum.SUCCESS, StpUtil.getLoginIdAsString());
        } else {
            // 返回空
            return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
        }
    }

    @PostMapping("/changePassword")
    public RestResponseVo<Void> changePassword(@NotNull @RequestParam String oldPassword,
                                               @NotNull @RequestParam String newPassword) {
        // 判断是否已经登陆
        if (!StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.NOT_LOG_IN);
        }
        userService.changePassword(oldPassword, newPassword);

        log.info("{} change password success", StpUtil.getLoginIdAsString());
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/ban")
    @SaCheckRole("ADMIN")
    public RestResponseVo<Void> ban(@NotNull @RequestParam String username,
                                    @NotNull @RequestParam int banDays) {
        userService.banUser(username, banDays);
        // 踢出用户
        StpUtil.kickout(username);
        // 打印信息
        log.info("{} ban success, days: {}", username, banDays);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/unban")
    @SaCheckRole("ADMIN")
    public RestResponseVo<Void> unban(@NotNull @RequestParam String username) {
        userService.unbanUser(username);
        // 打印信息
        log.info("{} unban success", username);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "进入游戏，将会修改上次在线时间，并且领取奖励")
    @PostMapping("/enterGameToReceiveReward")
    public RestResponseVo<Integer> enterGameToReceiveReward() {
        // 获取当前用户
        String username = StpUtil.getLoginIdAsString();
        // 领取奖励
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, userService.enterGameToReceiveReward(username));
    }
}
