package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.Response;
import com.shuidun.sandbox_town_backend.bean.User;
import com.shuidun.sandbox_town_backend.config.MySaTokenUtils;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @RequestMapping("/login")
    public Response<?> login(String username, String passwd, boolean rememberMe) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            return new Response<>(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        User user = userService.findUserByName(username);
        if (user == null) {
            return new Response<>(StatusCodeEnum.USER_NOT_EXIST);
        }
        String encryptedPasswd = MySaTokenUtils.encryptedPasswd(passwd, user.getSalt());
        if (encryptedPasswd.equals(user.getPasswd())) {
            StpUtil.login(username, rememberMe);
            log.info("{} login success, rememberMe: {}", StpUtil.getLoginId(), rememberMe);
            return new Response<>(StatusCodeEnum.SUCCESS);
        } else {
            return new Response<>(StatusCodeEnum.INCORRECT_CREDENTIALS);
        }
    }

    @RequestMapping("/signup")
    public Response<?> signup(String username, String passwd) {
        // 判断是否已经登陆
        if (StpUtil.isLogin()) {
            return new Response<>(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        // 判断密码强度
        if (passwd == null || passwd.length() < 6) {
            return new Response<>(StatusCodeEnum.PASSWORD_TOO_SHORT);
        }
        // 生成盐和加密后的密码
        String[] saltAndPasswd = MySaTokenUtils.generateSaltedHash(passwd);
        try {
            User user = new User(username, saltAndPasswd[1], saltAndPasswd[0]);
            userService.signup(user);
            StpUtil.login(username);
        } catch (DataIntegrityViolationException e) {
            return new Response<>(StatusCodeEnum.USER_ALREADY_EXIST);
        }
        return new Response<>(StatusCodeEnum.SUCCESS);
    }

    @RequestMapping("/logout")
    public Response<?> logout() {
        StpUtil.logout();
        return new Response<>(StatusCodeEnum.SUCCESS);
    }


    @GetMapping("/tokenInfo")
    public Response<?> tokenInfo() {
        return new Response<>(StatusCodeEnum.SUCCESS, StpUtil.getTokenInfo());
    }

    @GetMapping("/roleList")
    public Response<?> roleList() {
        var roleList = StpUtil.getRoleList();
        return new Response<>(StatusCodeEnum.SUCCESS, roleList);
    }

    /**
     * 强制注销 和 踢人下线 的区别在于：
     * 强制注销等价于对方主动调用了注销方法，再次访问会提示：Token无效。
     * 踢人下线不会清除Token信息，而是将其打上特定标记，再次访问会提示：Token已被踢下线。
     */
    @SaCheckRole("admin")
    @GetMapping("/kickout")
    public Response<?> kickout(String userName) {
        StpUtil.kickout(userName);
        return new Response<>(StatusCodeEnum.SUCCESS);
    }

    @GetMapping("/changePassword")
    public Response<?> changePassword(String oldPassword, String newPassword) {
        // public Response<?> changePassword(String oldPassword, String newPassword) {
        // String username = StpUtil.getLoginIdAsString();
        // User user = userService.getUserByUsername(username);
        // String salt = user.getSalt();
        // String oldPasswd = new SimpleHash(Sha256Hash.ALGORITHM_NAME, oldPassword, salt, 3).toBase64();
        // if (!oldPasswd.equals(user.getPassword())) {
        //     return new Response<>(StatusCodeEnum.INCORRECT_CREDENTIALS);
        // }
        // String newPasswd = new SimpleHash(Sha256Hash.ALGORITHM_NAME, newPassword, salt, 3).toBase64();
        // user.setPassword(newPasswd);
        // userService.updateUser(user);

        return new Response<>(StatusCodeEnum.SUCCESS);
    }
}
