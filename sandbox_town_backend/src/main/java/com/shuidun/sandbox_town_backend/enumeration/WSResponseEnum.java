package com.shuidun.sandbox_town_backend.enumeration;

import com.shuidun.sandbox_town_backend.bean.WSResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 服务器向客户端发送的事件类型
@Getter
@AllArgsConstructor
public enum WSResponseEnum {
    // 移动
    // {"id": "user_xixi", "speed": 1, "path": [1,3,4,9,7,10]}
    MOVE,
}
