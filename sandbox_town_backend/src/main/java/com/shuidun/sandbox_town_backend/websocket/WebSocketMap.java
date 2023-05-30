package com.shuidun.sandbox_town_backend.websocket;

import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketMap {
    private static final Map<String, WebSocketSession> userNameWebSession = new ConcurrentHashMap<>();

    public static void setSessionByUsername(String userName, WebSocketSession webSocketSession) {
        userNameWebSession.put(userName, webSocketSession);
    }

    /**
     * 根据username获取其WebSocketSession
     */
    public static WebSocketSession getSessionByUserName(String userName) {
        return userNameWebSession.get(userName);
    }

    /**
     * 移除WebSocketSession
     */
    public static void removeWebSocketSession(String userName, WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return;
        }
        userNameWebSession.remove(userName, webSocketSession);
    }

    /**
     * 得到用户列表
     */
    public static Set<String> getUserList() {
        return userNameWebSession.keySet();
    }

    /**
     * 得到session列表
     */
    public static Collection<WebSocketSession> getSessionList() {
        return userNameWebSession.values();
    }

    /**
     * 得到键值对
     */
    public static Set<Map.Entry<String, WebSocketSession>> getEntrySet() {
        return userNameWebSession.entrySet();
    }
}