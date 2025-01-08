package com.shuidun.sandbox_town_backend.enumeration;

/** 服务器向客户端发送的事件类型 */
public enum WSResponseEnum {
    /** 移动 */
    MOVE,
    /** 下线 */
    OFFLINE,
    /** 时间段通知 */
    TIME_FRAME_NOTIFY,
    /** 精灵属性变化通知 */
    SPRITE_ATTRIBUTE_CHANGE,
    /** 精灵缓存失效通知 */
    SPRITE_CACHE_INVALIDATE,
    /** 精灵HP变化通知 */
    SPRITE_HP_CHANGE,
    /** 驯服结果通知 */
    FEED_RESULT,
    /** 获得或消耗物品通知 */
    ITEM_GAIN,
    /** 聊天消息通知 */
    CHAT_MESSAGE,
}
