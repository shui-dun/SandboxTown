package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.Response;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/foo")
    public Response<?> foo() {
        return new Response<>(StatusCodeEnum.SUCCESS, "foo");
    }

    @RequestMapping("/login")
    public Response<?> login(String username, String passwd, boolean rememberMe) {
        if (StpUtil.isLogin()) {
            return new Response<>(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        if ("123".equals(passwd)) {
            StpUtil.login(username, rememberMe);
            log.info("{} login success, rememberMe: {}", StpUtil.getLoginId(), rememberMe);
            return new Response<>(StatusCodeEnum.SUCCESS, "foo");
        } else {
            return new Response<>(StatusCodeEnum.INCORRECT_CREDENTIALS);
        }
        // try {
        //     Subject subject = SecurityUtils.getSubject();
        //     AuthenticationToken token = new UsernamePasswordToken(username, passwd);
        //     subject.login(token);
        //     return new Response<>(StatusCodeEnum.SUCCESS);
        // } catch (UnknownAccountException e) {
        //     return new Response<>(StatusCodeEnum.USER_NOT_EXIST);
        // } catch (IncorrectCredentialsException e) {
        //     return new Response<>(StatusCodeEnum.INCORRECT_CREDENTIALS);
        // } catch (Exception e) {
        //     return new Response<>(StatusCodeEnum.SERVER_ERROR);
        // }
    }

    @RequestMapping("/signup")
    public Response<?> signup(String username, String passwd) {
        if (StpUtil.isLogin()) {
            return new Response<>(StatusCodeEnum.ALREADY_LOGGED_IN);
        }
        // String salt = new SecureRandomNumberGenerator().nextBytes().toString();
        // String newPasswd = new SimpleHash(Sha256Hash.ALGORITHM_NAME, passwd, salt, 3).toBase64();
        // User user = new User(username, newPasswd, salt);
        // try {
        //     userService.signup(user);
        //     Subject subject = SecurityUtils.getSubject();
        //     AuthenticationToken token = new UsernamePasswordToken(username, passwd);
        //     subject.login(token);
        // } catch (DataIntegrityViolationException e) {
        //     return new Response<>(StatusCodeEnum.USER_ALREADY_EXIST);
        // }
        return new Response<>(StatusCodeEnum.SUCCESS);
    }

    @RequestMapping("/logout")
    public Response<?> logout() {
        // Subject subject = SecurityUtils.getSubject();
        // if (subject.isAuthenticated()) {
        //     subject.logout();
        // }
        if (StpUtil.isLogin()) {
            log.info("isLogin");
            StpUtil.logout();
        }
        return new Response<>(StatusCodeEnum.SUCCESS);
    }

    // 根据用户名删除用户
    @RequestMapping("/delete/{name}")
    public Response<?> delete(@PathVariable String name) {
        // try {
        //     int ans = userService.deleteNotAdminUser(name);
        //     if (ans == 0) {
        //         return new Response<>(StatusCodeEnum.USER_NOT_EXIST);
        //     } else {
        //         return new Response<>(StatusCodeEnum.SUCCESS);
        //     }
        // } catch (UnauthorizedException e) {
        //     return new Response<>(StatusCodeEnum.UNAUTHORIZED);
        // }
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

    @GetMapping("/permList")
    public Response<?> permList() {
        var permList = StpUtil.getPermissionList();
        return new Response<>(StatusCodeEnum.SUCCESS, permList);
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
