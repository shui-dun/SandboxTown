package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

/**
 * 精灵缓存信息（只存放在Java内存，而不在数据库&Redis中）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteCache {
    /** 只有x和y会定期写入数据库，其他的都只在Java内存中 */
    private Double x;

    private Double y;

    private Double vx;

    private Double vy;

    /** 上次更新坐标时间 */
    private Long lastMoveTime;

    /** 上次与其他精灵交互时间 */
    @Nullable
    private Long lastInteractTime;

    /** 上次与其他精灵交互的序列号 */
    @Nullable
    private Integer lastInteractSn;

    private SpriteStatus status;

    /** 目标精灵id */
    @Nullable
    private String targetSpriteId;

    /** 目标建筑id */
    @Nullable
    private String targetBuildingId;

    /** 目标（既非精灵也非建筑）的x坐标 */
    @Nullable
    private Double targetX;

    /** 目标（既非精灵也非建筑）的y坐标 */
    @Nullable
    private Double targetY;
}
