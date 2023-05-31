package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.EventMessage;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import com.shuidun.sandbox_town_backend.observer.ObserverNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket消息处理器
 */
@Service
@Slf4j
public class EventWebSocketHandler extends TextWebSocketHandler {

    /**
     * 建立连接后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userName = (String) session.getAttributes().get("userName");
        // 如果用户已经存在，删除之前的session
        if (WSManager.usernameSession.containsKey(userName)) {
            WebSocketSession webSocketSession = WSManager.usernameSession.get(userName);
            if (webSocketSession.isOpen()) {
                webSocketSession.close();
            }
        }
        // 保存用户session
        WSManager.usernameSession.put(userName, session);
        log.info("call afterConnectionEstablished");
    }

    /**
     * 任何原因导致WebSocket连接中断时，会自动调用afterConnectionClosed方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("call afterConnectionClosed");
        String userName = (String) session.getAttributes().get("userName");
        WSManager.usernameSession.remove(userName, session);
        // 发出下线事件
        EventMessage eventMessage = new EventMessage(EventEnum.OFFLINE, userName, null);
        ObserverNotifier.notify(eventMessage);
        super.afterConnectionClosed(session, status);
    }

    /**
     * 收到消息时进行的操作（转发给目标用户）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        String messagePayload = message.getPayload();
        if ("".equals(messagePayload)) {
            return;
        }
        EventMessage eventMessage = JSONObject.parseObject(messagePayload, EventMessage.class);
        eventMessage.setInitiator((String) session.getAttributes().get("userName"));
        log.info("收到来自用户{}的消息：{}", session.getAttributes().get("userName"), eventMessage);
        // 通知给观察者
        ObserverNotifier.notify(eventMessage);
    }

}
