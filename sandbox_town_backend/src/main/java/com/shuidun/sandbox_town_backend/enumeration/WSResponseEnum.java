package com.shuidun.sandbox_town_backend.enumeration;

import com.shuidun.sandbox_town_backend.bean.WSResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 服务器向客户端发送的事件类型
@Getter
@AllArgsConstructor
public enum WSResponseEnum {
    // 更新坐标
    // {"id": "user_xixi", "x": 1, "y": 2, "vx": 3.0, "vy": 2.2}
    COORDINATE,
    // 移动
    // {"id": "user_xixi", "speed": 1, "path": [1,3,4,9,7,10], "dest_id": "store_1"}
    MOVE,
    // 上线
    // {"id": "user_xixi", "x": 1, "y": 2, "type": "user", "level": 1, "owner": null, ...}
    ONLINE,
    // 下线
    // {"id": "user_xixi"}
    OFFLINE,
}
