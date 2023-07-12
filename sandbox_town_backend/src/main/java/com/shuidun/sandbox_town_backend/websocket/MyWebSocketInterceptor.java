package com.shuidun.sandbox_town_backend.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * 握手阶段的拦截器，对连接进行过滤，可以对连接前和连接后自定义处理
 */
@Service
@Slf4j
public class MyWebSocketInterceptor extends HttpSessionHandshakeInterceptor {
    /**
     * 握手前
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        log.info("call beforeHandshake");
        // 先要通过sa-token判断用户是否登录
        if (!StpUtil.isLogin()) {
            log.error("user not login");
            return false;
        }
        // 将用户名放入ws session属性列表
        attributes.put("userName", StpUtil.getLoginIdAsString());
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        log.info("call afterHandshake");
        super.afterHandshake(request, response, wsHandler, ex);
    }

}