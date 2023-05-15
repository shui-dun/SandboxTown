package com.shuidun.sandbox_town_backend.config;

import com.shuidun.sandbox_town_backend.ws.EventWebSocketHandler;
import com.shuidun.sandbox_town_backend.ws.EventWebSocketInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final EventWebSocketHandler eventWebSocketHandler;

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
        // registry.addHandler(eventWebSocketHandler, "/event");
        registry.addHandler(eventWebSocketHandler, "/sockjs").addInterceptors(eventWebSocketInterceptor).setAllowedOrigins("*").withSockJS();
    }

}

