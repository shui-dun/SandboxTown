package com.shuidun.sandbox_town_backend.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static java.util.Map.entry;


@Slf4j
public class WSMessageSender {
    public static final Map<String, WebSocketSession> usernameSession = new ConcurrentHashMap<>();

    /**
     * 发送消息给指定用户
     */
    private static void sendMessageToUser(String username, WSResponseVo response) {
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
     * 发送消息给当前用户
     */
    private static void sendMessageToCurrentUser(WSResponseVo response) {
        sendMessageToUser(StpUtil.getLoginIdAsString(), response);
    }


    /**
     * 发送消息给所有用户
     */
    private static void sendMessageToAllUsers(WSResponseVo response) {
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

    /**
     * 发送消息给目标用户（即消息中id字段指定的用户）
     */
    private static void sendMessageToTargetUser(WSResponseVo response) {
        try {
            // 使用反射获取getId()的值
            Method getIdMethod = response.getData().getClass().getDeclaredMethod("getId");
            String targetUserId = (String) getIdMethod.invoke(response.getData());
            if (!targetUserId.startsWith("USER_")) {
                return;
            }
            sendMessageToUser(targetUserId, response);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    private static Map<WSResponseEnum, Consumer<WSResponseVo>> responseSendFunctionMap = Map.ofEntries(
            entry(WSResponseEnum.COORDINATE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.MOVE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.OFFLINE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.TIME_FRAME_NOTIFY, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.ITEM_BAR_NOTIFY, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.SPRITE_EFFECT_CHANGE, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.SPRITE_HP_CHANGE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.FEED_RESULT, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.ITEM_GAIN, WSMessageSender::sendMessageToTargetUser)
    );

    /**
     * 处理响应，发送给应该接收到该事件的用户
     */
    public static void sendResponse(WSResponseVo response) {
        var responseEnum = response.getType();
        var sendFunction = responseSendFunctionMap.get(responseEnum);
        if (sendFunction != null) {
            sendFunction.accept(response);
        } else {
            log.error("未知的响应类型：{}", responseEnum);
        }
    }

    /**
     * 处理响应列表，发送给应该接收到该事件的用户
     */
    public static void sendResponseList(List<WSResponseVo> responseList) {
        for (var response : responseList) {
            sendResponse(response);
        }
    }
}
