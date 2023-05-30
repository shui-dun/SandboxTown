package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.Event;
import com.shuidun.sandbox_town_backend.enumeration.EventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

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
        WebSocketMap.setSessionByUsername(userName, session);
        log.info("call afterConnectionEstablished");
    }

    /**
     * 任何原因导致WebSocket连接中断时，会自动调用afterConnectionClosed方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("call afterConnectionClosed");
        String userName = (String) session.getAttributes().get("userName");
        WebSocketMap.removeWebSocketSession(userName, session);
        super.afterConnectionClosed(session, status);
    }

    /**
     * 收到消息时进行的操作（转发给目标用户）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到websocket消息: {}", message.toString());
        super.handleTextMessage(session, message);
        String messagePayload = message.getPayload();
        if (!"".equals(messagePayload)) {
            Event event = JSONObject.parseObject(messagePayload, Event.class);
            log.info(event.toString());
            Event responseEvent = new Event(EventEnum.FOO, Map.of("x", 1, "y", 2));
            sendMessageToAllUsers(new TextMessage(JSONObject.toJSONString(responseEvent)));
        }

    }

    /**
     * 发送消息给所有用户
     */
    public void sendMessageToAllUsers(AbstractWebSocketMessage<?> message) {
        // 遍历所有用户session的键值对
        for (Map.Entry<String, WebSocketSession> entry : WebSocketMap.getEntrySet()) {
            // 用户名
            String userName = entry.getKey();
            // 会话
            WebSocketSession session = entry.getValue();
            try {
                if (!session.isOpen()) {
                    WebSocketMap.removeWebSocketSession(userName, session);
                } else {
                    session.sendMessage(message);
                    log.info("发送session{}消息: {}", session, message.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
