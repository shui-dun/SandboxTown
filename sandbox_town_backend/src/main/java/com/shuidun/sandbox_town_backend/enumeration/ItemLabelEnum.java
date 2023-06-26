package com.shuidun.sandbox_town_backend.enumeration;

/**
 * 物品的标签包含FOOD（可食用）、USABLE（用品）、WEAPON（武器）、HELMET（头盔）, CHEST（胸甲）, LEG（腿甲）, BOOTS（鞋）
 * 注意：所以东西都可以手持，所以东西都可以手持，WEAPON（武器）只是给前端的一个分类
 * 注意：FOOD（可食用）、USABLE（用品）实际上也是一样的，就是可直接使用的物品，只是给前端的细分分类
 * 注意：在前端：HELMET（头盔）, CHEST（胸甲）, LEG（腿甲）, BOOTS（鞋）合并显示为装备
 */
public enum ItemLabelEnum {
    FOOD,
    USABLE,
    WEAPON,
    HELMET,
    CHEST,
    LEG,
    BOOTS
}
