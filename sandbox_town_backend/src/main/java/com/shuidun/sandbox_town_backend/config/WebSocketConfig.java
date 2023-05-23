package com.shuidun.sandbox_town_backend.config;

import com.shuidun.sandbox_town_backend.websocket.EventWebSocketHandler;
import com.shuidun.sandbox_town_backend.websocket.EventWebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /** websocket处理器 */
    private final EventWebSocketHandler eventWebSocketHandler;

    /** websocket拦截器 */
    private final EventWebSocketInterceptor eventWebSocketInterceptor;

    public WebSocketConfig(EventWebSocketHandler eventWebSocketHandler, EventWebSocketInterceptor eventWebSocketInterceptor) {
        this.eventWebSocketHandler = eventWebSocketHandler;
        this.eventWebSocketInterceptor = eventWebSocketInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 配置handle
        // 配置websocket的监听url
        // 配置拦截器
        registry.addHandler(eventWebSocketHandler, "/event").addInterceptors(eventWebSocketInterceptor).setAllowedOrigins("*");
        registry.addHandler(eventWebSocketHandler, "/sockjs").addInterceptors(eventWebSocketInterceptor).setAllowedOrigins("*").withSockJS();
    }

}

