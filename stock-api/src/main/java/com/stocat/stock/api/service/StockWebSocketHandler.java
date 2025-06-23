package com.stocat.stock.api.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket 클라이언트 연결을 처리하고, Redis에서 받은 체결 데이터를 전송합니다.
 */
@Component
public class StockWebSocketHandler implements WebSocketHandler {

    private final RedisTradeSubscriber subscriber;

    public StockWebSocketHandler(RedisTradeSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * 클라이언트 연결 시, Redis로부터 실시간 JSON 메시지를 받아 WebSocket 메시지로 전송합니다.
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
                subscriber.priceFlux()
                        .map(session::textMessage)
        );
    }
}
