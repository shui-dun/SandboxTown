package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.service.UserService;
import com.shuidun.sandbox_town_backend.utils.MySaTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
        User user = userService.findUserByName(username);
        // 判断用户是否存在
        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 用户是否被封禁
        if (userService.isUserBanned(username)) {
            throw new BusinessException(StatusCodeEnum.USER_BEEN_BANNED);
        }
        // 判断密码是否正确
        String encryptedPasswd = MySaTokenUtils.encryptedPasswd(password, user.getSalt());
        if (encryptedPasswd.equals(user.getPassword())) {
            StpUtil.login(username, rememberMe);
            log.info("{} login success, rememberMe: {}", username, rememberMe);
            return new RestResponse<>(StatusCodeEnum.SUCCESS);
        } else {
            throw new BusinessException(StatusCodeEnum.INCORRECT_CREDENTIALS);
        }
    }

    @PostMapping("/signup")
    public RestResponse<?> signup(String usernameSuffix, String password) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        // 判断用户名是否合法
        if (usernameSuffix == null || usernameSuffix.length() < 3) {
            throw new BusinessException(StatusCodeEnum.USERNAME_TOO_SHORT);
        }
        // 判断密码强度
        if (password == null || password.length() < 6) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 生成盐和加密后的密码
        String[] saltAndPasswd = MySaTokenUtils.generateSaltedHash(password);
        // 用户名为"user_" + usernameSuffix
        String username = "user_" + usernameSuffix;
        try {
            User user = new User(username, saltAndPasswd[1], saltAndPasswd[0]);
            userService.createUser(user);
            StpUtil.login(username);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(StatusCodeEnum.USER_ALREADY_EXIST);
        }
        // 打印注册信息
        log.info("{} signup success", username);
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @PostMapping("/logout")
    public RestResponse<?> logout() {
        StpUtil.logout();
        return new RestResponse<>(StatusCodeEnum.SUCCESS);
    }

    @GetMapping("/getUsername")
    public RestResponse<?> getUsername() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, StpUtil.getLoginIdAsString());
    }

    @PostMapping("/changePassword")
    public RestResponse<?> changePassword(String oldPassword, String newPassword) {
        // 判断是否已经登陆
        if (!StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.NOT_LOG_IN);
        }
        // 判断密码强度
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 获取当前用户
        String username = StpUtil.getLoginIdAsString();
        User user = userService.findUserByName(username);
        // 判断密码是否正确
        String encryptedPasswd = MySaTokenUtils.encryptedPasswd(oldPassword, user.getSalt());
        if (encryptedPasswd.equals(user.getPassword())) {
            // 生成盐和加密后的密码
            String[] saltAndPasswd = MySaTokenUtils.generateSaltedHash(newPassword);
            user.setSalt(saltAndPasswd[0]);
            user.setPassword(saltAndPasswd[1]);
            userService.updateUser(user);
            // 打印信息
            log.info("{} change password success", username);
            return new RestResponse<>(StatusCodeEnum.SUCCESS);
        } else {
            throw new BusinessException(StatusCodeEnum.INCORRECT_CREDENTIALS);
        }
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
}
