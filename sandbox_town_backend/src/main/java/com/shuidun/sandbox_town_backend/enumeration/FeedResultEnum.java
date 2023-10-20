package com.shuidun.sandbox_town_backend.enumeration;

/**
 * 喂养&驯服精灵的结果
 */
public enum FeedResultEnum {
    /** 驯服成功 */
    TAME_SUCCESS,
    /** 驯服失败 */
    TAME_FAIL,
    /** 精灵已经被其他人驯服 */
    ALREADY_TAMED,
    /** 该精灵无法被驯服 */
    CANNOT_TAMED,
    /** 没有驯服所需的物品 */
    NO_ITEM,
    /** 喂养成功 */
    FEED_SUCCESS,
}
