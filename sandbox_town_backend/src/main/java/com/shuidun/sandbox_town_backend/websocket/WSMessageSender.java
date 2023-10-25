package com.shuidun.sandbox_town_backend.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import static java.util.Map.entry;


@Slf4j
public class WSMessageSender {
    public static final Map<String, WebSocketSession> usernameSession = new ConcurrentHashMap<>();

    /** 尚未被整理（即尚不确定应该发给谁）的消息队列 */
    private static final LinkedBlockingQueue<WSResponseVo> mq = new LinkedBlockingQueue<>();

    /** 等待发送的消息队列列表 */
    private static final List<LinkedBlockingQueue<Pair<WebSocketSession, TextMessage>>> sendQueues;

    /**
     * websocket消息发送线程池大小
     * 按理说这个配置应该放在application.yml中，但是springboot里面静态类无法注入配置，所以只能放在这里
     */
    private static final int wsSenderTheadPoolSize = 3;

    static {
        sendQueues = new ArrayList<>(wsSenderTheadPoolSize);
        for (int i = 0; i < wsSenderTheadPoolSize; i++) {
            sendQueues.add(new LinkedBlockingQueue<>());
        }
        sendMessages();
    }

    /** 发送消息 */
    private static void sendMessages() {
        // 处理尚未被整理（即尚不确定应该发给谁）的消息队列
        new Thread(() -> {
            while (true) {
                WSResponseVo response = null;
                try {
                    response = mq.take();
                    var responseEnum = response.getType();
                    var sendFunction = responseSendFunctionMap.get(responseEnum);
                    if (sendFunction != null) {
                        sendFunction.accept(response);
                    } else {
                        log.error("未知的响应类型：{}", responseEnum);
                    }
                } catch (Exception e) {
                    log.error("handle {} event error", response, e);
                }
            }
        }).start();

        // 处理等待发送的消息队列列表（真正地发送消息）
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(wsSenderTheadPoolSize);
        for (int i = 0; i < wsSenderTheadPoolSize; i++) {
            int finalI = i;
            fixedThreadPool.execute(() -> {
                while (true) {
                    Pair<WebSocketSession, TextMessage> pair = null;
                    try {
                        pair = sendQueues.get(finalI).take();
                        pair.getFirst().sendMessage(pair.getSecond());
                    } catch (Exception e) {
                        log.error("send message {} error", pair, e);
                    }
                }
            });
        }
    }

    /**
     * 发送消息给指定用户
     */
    private static void sendMessageToUser(String username, TextMessage message) {
        // 得到会话
        WebSocketSession session = WSMessageSender.usernameSession.get(username);
        try {
            if (!session.isOpen()) {
                WSMessageSender.usernameSession.remove(username, session);
            } else {
                // 计算应该放入哪个队列
                // 使用hashcode，保证同一个用户的消息放入同一个队列，这样是为了防止如下报错：
                // java.lang.IllegalStateException: The remote endpoint was in state [TEXT_PARTIAL_WRITING] which is an invalid state for called method
                // 该报错指的是在没有完成当前消息发送的情况下就试图发送新的消息。
                // 在WebSocket中，你不能对同一个session同时发送多个消息，必须等待当前的消息发送完成后才能发送下一个消息
                int queueIndex = Math.abs(session.hashCode()) % wsSenderTheadPoolSize;
                sendQueues.get(queueIndex).put(Pair.of(session, message));
            }
        } catch (Exception e) {
            log.info("send message to user {} error: {}", username, e.getMessage());
        }
    }

    /**
     * 发送消息给所有用户
     */
    private static void sendMessageToAllUsers(WSResponseVo response) {
        var message = new TextMessage(JSONObject.toJSONString(response));
        // 遍历所有用户
        for (String userName : WSMessageSender.usernameSession.keySet()) {
            sendMessageToUser(userName, message);
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
            var message = new TextMessage(JSONObject.toJSONString(response));
            sendMessageToUser(targetUserId, message);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /** 响应类型 -> 发送函数 */
    private static final Map<WSResponseEnum, Consumer<WSResponseVo>> responseSendFunctionMap = Map.ofEntries(
            entry(WSResponseEnum.COORDINATE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.MOVE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.OFFLINE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.TIME_FRAME_NOTIFY, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.ITEM_BAR_NOTIFY, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.SPRITE_EFFECT_CHANGE, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.SPRITE_HP_CHANGE, WSMessageSender::sendMessageToAllUsers),
            entry(WSResponseEnum.FEED_RESULT, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.ITEM_GAIN, WSMessageSender::sendMessageToTargetUser),
            entry(WSResponseEnum.CHAT_MESSAGE, WSMessageSender::sendMessageToTargetUser)
    );

    /**
     * 将响应添加到消息队列
     */
    public static void addResponse(WSResponseVo response) {
        mq.add(response);
    }

    /**
     * 将多个响应添加到消息队列
     */
    public static void addResponses(List<WSResponseVo> responseList) {
        for (var response : responseList) {
            addResponse(response);
        }
    }
}
