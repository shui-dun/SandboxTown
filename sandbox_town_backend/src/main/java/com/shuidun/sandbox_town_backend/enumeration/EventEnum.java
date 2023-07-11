package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/** 客户端向服务器发送的事件类型 */
@AllArgsConstructor
@Getter
public enum EventEnum {
    // 下线
    OFFLINE,
    // 告知坐标信息
    COORDINATE,
    // 想要移动到某个位置
    MOVE,
}
