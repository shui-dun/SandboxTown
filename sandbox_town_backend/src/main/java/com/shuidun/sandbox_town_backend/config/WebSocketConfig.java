package com.shuidun.sandbox_town_backend.config;

import com.shuidun.sandbox_town_backend.websocket.MyWebSocketHandler;
import com.shuidun.sandbox_town_backend.websocket.MyWebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /** websocket处理器 */
    private final MyWebSocketHandler myWebSocketHandler;

    /** websocket拦截器 */
    private final MyWebSocketInterceptor myWebSocketInterceptor;

    public WebSocketConfig(MyWebSocketHandler myWebSocketHandler, MyWebSocketInterceptor myWebSocketInterceptor) {
        this.myWebSocketHandler = myWebSocketHandler;
        this.myWebSocketInterceptor = myWebSocketInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 配置handle
        // 配置websocket的监听url
        // 配置拦截器
        registry.addHandler(myWebSocketHandler, "/websocket").addInterceptors(myWebSocketInterceptor).setAllowedOrigins("*");
        registry.addHandler(myWebSocketHandler, "/sockjs").addInterceptors(myWebSocketInterceptor).setAllowedOrigins("*").withSockJS();
    }

}

