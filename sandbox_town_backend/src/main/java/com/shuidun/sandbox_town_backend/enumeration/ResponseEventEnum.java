package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 服务器向客户端发送的事件类型 */
@AllArgsConstructor
@Getter
public enum ResponseEventEnum {
    FOO("FOO");
    private final String type;
}
