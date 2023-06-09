package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 服务器向客户端发送的事件类型 */
@Getter
@AllArgsConstructor
public enum WSResponseEnum {
    /** 更新坐标 */
    COORDINATE,
    /** 移动 */
    MOVE,
    /** 上线 */
    ONLINE,
    /** 下线 */
    OFFLINE,
    /** 时间段通知 */
    TIME_FRAME_NOTIFY,
    /** 物品栏通知 */
    ITEM_BAR_NOTIFY,
    /** 精灵属性变化通知 */
    SPRITE_ATTRIBUTE_CHANGE,
}
