package com.shuidun.sandbox_town_backend.ws;

import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketUser {
    private static final Map<String, List<WebSocketSession>> userNameWebSession = new ConcurrentHashMap<>();

    public static void add(String userName, WebSocketSession webSocketSession) {
        userNameWebSession.computeIfAbsent(userName, v -> new ArrayList<>()).add(webSocketSession);
    }

    /**
     * 根据username获取其WebSocketSession
     */
    public static List<WebSocketSession> getSessionByUserName(String userName) {
        return userNameWebSession.get(userName);
    }

    /**
     * 移除WebSocketSession
     */
    public static void removeWebSocketSession(String userName, WebSocketSession webSocketSession) {
        if (webSocketSession == null) {
            return;
        }
        List<WebSocketSession> webSocketSessions = userNameWebSession.get(userName);
        if (webSocketSessions == null || webSocketSessions.isEmpty()) {
            return;
        }
        webSocketSessions.remove(webSocketSession);
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
    public static Collection<List<WebSocketSession>> getSessionList() {
        return userNameWebSession.values();
    }
}