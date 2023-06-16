package com.shuidun.sandbox_town_backend.mixin;

import com.shuidun.sandbox_town_backend.bean.Point;
import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameInfo {
    // 角色坐标信息，保存在内存中，定期写入数据库
    public static Map<String, Point> spriteAxis = new ConcurrentHashMap<>();

    // 角色状态信息，保存在内存中，不写入数据库（暂时还没用到）
    public static Map<String, SpriteStatus> spriteStatus = new ConcurrentHashMap<>();
}
