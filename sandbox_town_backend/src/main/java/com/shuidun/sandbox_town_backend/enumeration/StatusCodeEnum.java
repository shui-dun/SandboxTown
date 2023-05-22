package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusCodeEnum {
    SUCCESS(0, "成功"),
    USER_NOT_EXIST(1, "用户不存在"),
    INCORRECT_CREDENTIALS(2, "密码错误"),
    SERVER_ERROR(3, "服务端错误"),
    UNAUTHENTICATED(4, "未登录"),
    USER_ALREADY_EXIST(5, "用户名已经存在"),
    UNAUTHORIZED(6, "未授权的操作");

    private final int code;
    private final String msg;

}
