package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/** 客户端向服务器发送的事件类型 */
@AllArgsConstructor
@Getter
public enum EventEnum {
    FOO("FOO");
    private final String type;

}
