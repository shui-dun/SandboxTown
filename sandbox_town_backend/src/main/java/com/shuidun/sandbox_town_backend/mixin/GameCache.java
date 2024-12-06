package com.shuidun.sandbox_town_backend.mixin;

import java.util.Random;

/**
 * 游戏缓存信息
 * 即，为了性能要求直接保存在内存（而非redis）中的信息
 * 例如：角色坐标信息
 * 这些信息有一些会定期写入数据库
 */
public class GameCache {
    /** 随机数产生器 */
    public static Random random = new Random();
}
