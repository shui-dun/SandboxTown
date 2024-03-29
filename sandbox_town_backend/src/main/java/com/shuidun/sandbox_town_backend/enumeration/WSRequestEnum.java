package com.shuidun.sandbox_town_backend.enumeration;

/** 客户端向服务器发送的事件类型 */
public enum WSRequestEnum {
    /** 告知坐标信息 */
    COORDINATE,
    /** 想要移动到某个位置 */
    MOVE,
    /** 精灵交互 */
    INTERACT,
    /** 索敌 */
    FIND_ENEMY,
}
