package com.shuidun.sandbox_town_backend.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * websocket消息处理器
 */
@Service
public class EventWebSocketHandler extends TextWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 建立连接后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("call afterConnectionEstablished");
    }

    /**
     * 任何原因导致WebSocket连接中断时，会自动调用afterConnectionClosed方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("call afterConnectionClosed");
        super.afterConnectionClosed(session, status);
    }

    /**
     * 收到消息时进行的操作（转发给目标用户）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("收到websocket消息: {}", message.toString());
        super.handleTextMessage(session, message);
    }
}