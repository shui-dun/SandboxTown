package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveVo {
    private String id;
    private int speed;
    private List<Integer> path;
    @Nullable
    private String destBuildingId;
    @Nullable
    private String destSpriteId;
    /**
     * 序列号（serial number）
     * 由于精灵交互事件前总是伴随着移动事件，为了避免重复交互，使用移动事件的序列号来作为交互事件的序列号以去重
     * 只有移动事件具有目标精灵时，才会指定序列号，否则为null
     */
    @Nullable
    private Integer sn;
}
