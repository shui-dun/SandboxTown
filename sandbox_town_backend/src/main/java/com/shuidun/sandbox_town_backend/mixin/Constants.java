package com.shuidun.sandbox_town_backend.mixin;

import org.springframework.beans.factory.annotation.Value;

public class Constants {
    /** 多少经验升一级 */
    public static final int EXP_PER_LEVEL = 100;

    /** 地图上一格多少像素 */
    public static final int PIXELS_PER_GRID = 30;

    /** 物品栏大小 */
    public static final int ITEM_BAR_SIZE = 6;

    /** 白天时长5分钟（300000ms） */
    public static final int DAY_DURATION = 300000;

    /** 黄昏时长1分钟（60000ms） */
    public static final int DUSK_DURATION = 60000;

    /** 夜晚时长3分钟（180000ms） */
    public static final int NIGHT_DURATION = 180000;

    /** 黎明时长1分钟（60000ms） */
    public static final int DAWN_DURATION = 60000;

    /** 一天总时长 */
    public static final int DAY_TOTAL_DURATION = DAY_DURATION + DUSK_DURATION + NIGHT_DURATION + DAWN_DURATION;

    /** 白天开始时间 */
    public static final int DAY_START = 0;

    /** 黄昏开始时间 */
    public static final int DUSK_START = DAY_START + DAY_DURATION;

    /** 夜晚开始时间 */
    public static final int NIGHT_START = DUSK_START + DUSK_DURATION;

    /** 黎明开始时间 */
    public static final int DAWN_START = NIGHT_START + NIGHT_DURATION;

}
