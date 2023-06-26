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
    NOT_LOG_IN(4, "未登录"),
    USER_ALREADY_EXIST(5, "用户名已经存在"),
    NO_PERMISSION(6, "没有相应的权限"),
    ALREADY_LOGGED_IN(7, "已经登录"),
    PASSWORD_TOO_SHORT(8, "密码太短"),
    REQUEST_METHOD_NOT_SUPPORTED(9, "请求方法不支持"),
    USER_BEEN_BANNED(10, "用户已被封禁"),
    USERNAME_TOO_SHORT(11, "用户名太短"),
    ILLEGAL_ARGUMENT(12, "参数不合法"),
    ITEM_NOT_FOUND(13, "物品不存在"),
    ITEM_NOT_USABLE(14, "物品不可用"),
    PICK_APPLE_LIMIT_EXCEEDED(15, "采摘苹果次数超过限制"),
    TREE_APPLE_PICKED(16, "苹果已被采摘完了"),
    ITEM_NOT_ENOUGH(17, "物品数量不足"),
    MONEY_NOT_ENOUGH(18, "金钱不足");

    private final int code;
    private final String msg;

}
