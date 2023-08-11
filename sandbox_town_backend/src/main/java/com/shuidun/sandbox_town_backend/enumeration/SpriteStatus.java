package com.shuidun.sandbox_town_backend.enumeration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public enum SpriteStatus {
    // 空闲状态
    IDLE,
    // 前往某个精灵
    GO_TO_SPRITE,
    // 前往某个坐标
    GO_TO_COORDINATE,
    // 前往某个建筑
    GO_TO_BUILDING,
    // 正在交互
    INTERACTING,
}
