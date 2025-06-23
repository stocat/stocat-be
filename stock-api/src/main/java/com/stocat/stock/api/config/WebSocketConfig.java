package com.stocat.stock.api.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

/**
 * WebSocket 핸들러 어댑터를 등록합니다.
 */
@Configuration
@EnableWebFlux
public class WebSocketConfig {
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
