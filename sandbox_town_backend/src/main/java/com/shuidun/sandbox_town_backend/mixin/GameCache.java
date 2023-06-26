package com.shuidun.sandbox_town_backend.mixin;

import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.bean.SpriteCache;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;

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
    // 随机数产生器
    public static Random random = new Random();

    // 建筑类型图片
    public static Map<BuildingTypeEnum, BufferedImage> buildingTypesImages = new ConcurrentHashMap<>();

    /** 地图，用于寻路算法，0表示可以通过，1代表围墙，非0表示障碍物ID的哈希值 */
    public static int[][] map;

    // 角色缓存信息，保存在内存中，定期写入数据库
    public static Map<String, SpriteCache> spriteCacheMap = new ConcurrentHashMap<>();
}
