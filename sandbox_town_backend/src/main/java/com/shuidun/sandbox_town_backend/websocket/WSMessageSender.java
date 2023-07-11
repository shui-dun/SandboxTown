package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WSMessageSender {
    public static final Map<String, WebSocketSession> usernameSession = new ConcurrentHashMap<>();

    /**
     * 发送消息给指定用户
     */
    public static void sendMessageToUser(String username, WSResponseVo response) {
        var message = new TextMessage(JSONObject.toJSONString(response));
        // 得到会话
        WebSocketSession session = WSMessageSender.usernameSession.get(username);
        synchronized (session) {
            try {
                if (!session.isOpen()) {
                    WSMessageSender.usernameSession.remove(username, session);
                } else {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 发送消息给所有用户
     */
    public static void sendMessageToAllUsers(WSResponseVo response) {
        var message = new TextMessage(JSONObject.toJSONString(response));
        // 遍历所有用户session的键值对
        for (Map.Entry<String, WebSocketSession> entry : WSMessageSender.usernameSession.entrySet()) {
            // 用户名
            String userName = entry.getKey();
            // 会话
            WebSocketSession session = entry.getValue();
            // 之所以在这里上锁，是为了防止如下报错：
            // java.lang.IllegalStateException: The remote endpoint was in state [TEXT_PARTIAL_WRITING] which is an invalid state for called method
            // 该报错指的是在没有完成当前消息发送的情况下就试图发送新的消息。
            // 在WebSocket中，你不能同时发送多个消息，必须等待当前的消息发送完成后才能发送下一个消息
            // 因此使用同步（synchronized）来确保在发送新的消息之前已经完成了当前消息的发送
            synchronized (session) {
                try {
                    if (!session.isOpen()) {
                        WSMessageSender.usernameSession.remove(userName, session);
                    } else {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
