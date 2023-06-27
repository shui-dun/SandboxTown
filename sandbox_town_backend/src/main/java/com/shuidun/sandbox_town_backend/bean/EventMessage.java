package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 客户端向服务器发送的事件 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {
    private EventEnum type;

    // 事件发起者的用户名
    private String initiator;

    private Map<String, Object> data;

}
