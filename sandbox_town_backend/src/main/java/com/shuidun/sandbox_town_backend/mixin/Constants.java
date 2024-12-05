package com.shuidun.sandbox_town_backend.mixin;

public class Constants {
    /** 升级所需经验值的基数 */
    public static final int EXP_PER_LEVEL = 100;

    /** 精灵最大体力值 */
    public static final int MAX_HP = 100;

    /** 精灵最大等级 */
    public static final int MAX_LEVEL = 20;

    /** 精灵最大速度 */
    public static final int MAX_SPEED = 25;

    /** 玩家死亡时失去的金钱值 */
    public static final int MONEY_LOST_ON_DEATH = 120;

    /** 精灵升级时得到的金钱值 */
    public static final int MONEY_GAIN_ON_LEVEL_UP = 50;

    /** 精灵最大饥饿值 */
    public static final int MAX_HUNGER = 100;

    /** 精灵饥饿值的临界点（低于这个值就不会自动恢复体力） */
    public static final int HUNGER_THRESHOLD = 80;

    /** 物品的最大生命值 */
    public static final int MAX_ITEM_LIFE = 100;

    /** 地图上一格多少像素 */
    public static final int PIXELS_PER_GRID = 30;

    /** 物品栏大小 */
    public static final int ITEM_BAR_SIZE = 6;

    /** 游戏循环的时间间隔 */
    public static final int GAME_LOOP_INTERVAL = 50;

    /** 帧率 */
    public static final int FPS = 1000 / GAME_LOOP_INTERVAL;

    /** 执行一次生命效果的帧数 */
    public static final int LIFE_FRAMES = 12 * FPS;

    /** 执行一次烧伤效果的帧率 */
    public static final int BURN_FRAMES = 2 * FPS;

    /** 执行一次精灵行为的帧数 */
    public static final int SPRITE_ACTION_FRAMES = 1 * FPS;

    /** 保存一次坐标的帧数 */
    public static final int SAVE_COORDINATE_FRAMES = 1 * FPS;

    /** 减少饱腹值的帧数 */
    public static final int REDUCE_HUNGER_FRAMES = 20 * FPS;

    /** 恢复体力的帧数 */
    public static final int RECOVER_LIFE_FRAMES = 13 * FPS;

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

    /** 白天开始时间 */
    public static final long DAY_START = 0;

    /** 黄昏开始时间 */
    public static final long DUSK_START = DAY_START + DAY_DURATION;

    /** 夜晚开始时间 */
    public static final long NIGHT_START = DUSK_START + DUSK_DURATION;

    /** 黎明开始时间 */
    public static final long DAWN_START = NIGHT_START + NIGHT_DURATION;

}
