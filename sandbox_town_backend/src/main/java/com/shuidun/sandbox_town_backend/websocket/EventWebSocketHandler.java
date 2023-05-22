package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.EventBean;
import com.shuidun.sandbox_town_backend.bean.MessageBean;
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
        WebSocketUser.add(userName, session);
        log.info("call afterConnectionEstablished");
    }

    /**
     * 任何原因导致WebSocket连接中断时，会自动调用afterConnectionClosed方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("call afterConnectionClosed");
        String userName = (String) session.getAttributes().get("userName");
        WebSocketUser.removeWebSocketSession(userName, session);
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
            EventBean eventBean = JSONObject.parseObject(messagePayload, EventBean.class);
            // sendMessageToUser(messageBean.getTargetUserName(), message);
            log.info(eventBean.toString());
            MessageBean newMessage = new MessageBean("player2", MessageBean.OperationTypeEnum.MOVE, Map.of("x", 1, "y", 2));
            sendMessageToAllUsers(new TextMessage(JSONObject.toJSONString(newMessage)));
        }

    }

    /**
     * 发送消息给所有用户
     */
    public void sendMessageToAllUsers(AbstractWebSocketMessage<?> message) {
        for (var sessions : WebSocketUser.getSessionList()) {
            for (var session : sessions) {
                try {
                    if (!session.isOpen()) {
                        WebSocketUser.removeWebSocketSession("player1", session);
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
}