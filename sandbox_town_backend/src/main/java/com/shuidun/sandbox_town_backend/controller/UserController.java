package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public RestResponse<?> login(String username, String password, boolean rememberMe) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        userService.login(username, password);
        StpUtil.login(username, rememberMe);
        log.info("{} login success, rememberMe: {}", username, rememberMe);
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/signup")
    public RestResponse<?> signup(String usernameSuffix, String password) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        String username = userService.createUser(usernameSuffix, password);
        // 打印注册信息
        log.info("{} signup success", username);
        StpUtil.login(username);
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/logout")
    public RestResponse<?> logout() {
        StpUtil.logout();
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @GetMapping("/getUsername")
    public RestResponse<?> getUsername() {
        if (StpUtil.isLogin()) {
            return new RestResponse<>(StatusCodeEnum.SUCCESS, StpUtil.getLoginIdAsString());
        } else {
            // 返回空
            return new RestResponse<>(StatusCodeEnum.SUCCESS, null);
        }
    }

    @PostMapping("/changePassword")
    public RestResponse<?> changePassword(String oldPassword, String newPassword) {
        // 判断是否已经登陆
        if (!StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.NOT_LOG_IN);
        }
        userService.changePassword(oldPassword, newPassword);

        log.info("{} change password success", StpUtil.getLoginIdAsString());
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/ban")
    @SaCheckRole("admin")
    public RestResponse<?> ban(String username, int banDays) {
        userService.banUser(username, banDays);
        // 踢出用户
        StpUtil.kickout(username);
        // 打印信息
        log.info("{} ban success, days: {}", username, banDays);
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/unban")
    @SaCheckRole("admin")
    public RestResponse<?> unban(String username) {
        userService.unbanUser(username);
        // 打印信息
        log.info("{} unban success", username);
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    /** 进入游戏，将会修改上次在线时间，并且领取奖励 */
    @PostMapping("/enterGameToReceiveReward")
    public RestResponse<?> enterGameToReceiveReward() {
        // 获取当前用户
        String username = StpUtil.getLoginIdAsString();
        // 领取奖励
        return new RestResponse<>(StatusCodeEnum.SUCCESS, userService.enterGameToReceiveReward(username));
    }
}
