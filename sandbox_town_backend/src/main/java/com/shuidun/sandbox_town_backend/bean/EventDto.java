package com.shuidun.sandbox_town_backend.bean;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.enumeration.WSRequestEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

/** 客户端向服务器发送的事件 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    @Nullable
    private WSRequestEnum type;

    /** 事件发起者的用户名 */
    @Nullable
    private String initiator;

    @Nullable
    private JSONObject data;

}
