package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteCache {
    private double x;
    private double y;
    // 上次更新坐标时间
    private long lastUpdateTime;
    // 上次与其他精灵交互时间
    private long lastInteractTime;
    private double vx;
    private double vy;
    SpriteStatus status;
}
