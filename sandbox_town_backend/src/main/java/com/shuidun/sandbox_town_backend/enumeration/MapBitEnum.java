package com.shuidun.sandbox_town_backend.enumeration;

/**
 * 用于定义地图点的不同类型。
 * 每个枚举值代表地图上的一种特定元素，例如围墙
 * 一个地图点可以有一个或多个元素
 */
public enum MapBitEnum {
    PLACEHOLDER1, // 原先这是 WALL，但WALL被废弃了，如果未来有新的元素需要添加，可以替换该占位符
    BUILDING,
    SURROUNDING_GREEK_TEMPLE,
    SURROUNDING_TOMBSTONE
}
