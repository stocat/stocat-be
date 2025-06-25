package com.stocat.trade.websocket.api.websocket;


import com.stocat.trade.websocket.api.service.RedisSubscriberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CryptoWebSocketHandler implements WebSocketHandler {
    private final RedisSubscriberService subscriber;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
                subscriber.subscribeTrades()
                        .map(session::textMessage)
        );
    }
}