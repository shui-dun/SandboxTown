package com.shuidun.sandbox_town_backend.enumeration;

/**
 * 驯服精灵的结果
 */
public enum TameResultEnum {
    SUCCESS,
    FAIL,
    /** 精灵已经被其他人驯服 */
    ALREADY_TAMED,
    /** 该精灵无法被驯服 */
    CANNOT_TAMED,
    /** 没有驯服所需的物品 */
    NO_ITEM,
}
