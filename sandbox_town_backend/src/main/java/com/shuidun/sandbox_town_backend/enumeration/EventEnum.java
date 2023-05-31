package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/** 客户端向服务器发送的事件类型 */
@AllArgsConstructor
@Getter
public enum EventEnum {
    // 上线
    ONLINE,
    // 下线
    OFFLINE,
    // 告知坐标信息
    // data: {"x": 1, "y": 2}
    COORDINATE,
    // 想要移动到某个位置
    // data: {"x0": 1, "y0": 2, "x1": 3, "y1": 4}
    MOVE,

}
