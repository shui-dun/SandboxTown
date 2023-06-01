package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.WSResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WSManager {
    public static final Map<String, WebSocketSession> usernameSession = new ConcurrentHashMap<>();

    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(String username, WSResponse response) {
        var message = new TextMessage(JSONObject.toJSONString(response));
        // 得到会话
        WebSocketSession session = WSManager.usernameSession.get(username);
        try {
            if (!session.isOpen()) {
                WSManager.usernameSession.remove(username, session);
            } else {
                session.sendMessage(message);
                log.info("发送消息给用户: {} 消息内容: {}", username, response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送消息给所有用户
     */
    public static void sendMessageToAllUsers(WSResponse response) {
        var message = new TextMessage(JSONObject.toJSONString(response));
        // 遍历所有用户session的键值对
        for (Map.Entry<String, WebSocketSession> entry : WSManager.usernameSession.entrySet()) {
            // 用户名
            String userName = entry.getKey();
            // 会话
            WebSocketSession session = entry.getValue();
            try {
                if (!session.isOpen()) {
                    WSManager.usernameSession.remove(userName, session);
                } else {
                    session.sendMessage(message);
                    log.info("发送消息给用户: {} 消息内容: {}", userName, response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
