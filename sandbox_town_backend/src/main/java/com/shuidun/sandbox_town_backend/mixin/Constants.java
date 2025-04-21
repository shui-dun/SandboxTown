package com.shuidun.sandbox_town_backend.mixin;

public class Constants {
    /** 物品的最大生命值 */
    public static final int MAX_ITEM_LIFE = 100;

    /** 白天时长5分钟（300000ms） */
    public static final long DAY_DURATION = 300000;

    /** 黄昏时长1分钟（60000ms） */
    public static final long DUSK_DURATION = 60000;

    /** 夜晚时长3分钟（180000ms） */
    public static final long NIGHT_DURATION = 180000;

    /** 黎明时长1分钟（60000ms） */
    public static final long DAWN_DURATION = 60000;

    /** 一天总时长 */
    public static final long DAY_TOTAL_DURATION = DAY_DURATION + DUSK_DURATION + NIGHT_DURATION + DAWN_DURATION;

    public static final byte[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public static final byte[][] DIAGONAL_DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
    };

}
