package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 服务器向客户端发送的事件 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WSResponseVo {
    private WSResponseEnum type;

    private Object data;
}
