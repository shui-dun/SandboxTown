package com.shuidun.sandbox_town_backend.mixin;

import com.shuidun.sandbox_town_backend.bean.SpriteCache;
import com.shuidun.sandbox_town_backend.bean.TimeFrameVo;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏缓存信息
 * 即，为了性能要求直接保存在内存（而非redis）中的信息
 * 例如：角色坐标信息
 * 这些信息有一些会定期写入数据库
 */
public class GameCache {
    /** 随机数产生器 */
    public static Random random = new Random();

    /** 建筑类型图片 */
    public static Map<BuildingTypeEnum, BufferedImage> buildingTypesImages = new ConcurrentHashMap<>();

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

    /** 角色缓存信息，保存在内存中，部分信息例如坐标定期写入数据库 */
    public static Map<String, SpriteCache> spriteCacheMap = new ConcurrentHashMap<>();

    /** 当前时间段 */
    public static TimeFrameVo timeFrame = new TimeFrameVo();
}
