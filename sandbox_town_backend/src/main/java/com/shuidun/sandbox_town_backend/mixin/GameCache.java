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

    /**
     * 表示地图上每个点的元素类型
     * 使用位标记来表示每个点可能包含的多种元素
     * 参见{@link com.shuidun.sandbox_town_backend.enumeration.MapBitEnum}
     */
    public static int[][] map = new int[0][0];

    /**
     * 存储地图上每个点的建筑物hashcode
     * 如果地图点不包含建筑物，则该位置的值的后32位为0
     */
    public static int[][] buildingsHashCodeMap = new int[0][0];
}
