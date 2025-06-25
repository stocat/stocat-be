package com.stocat.trade.websocket.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RedisSubscriberService {
    private final ReactiveRedisMessageListenerContainer redisContainer;
    private final ChannelTopic cryptoTradesTopic;

    /**
     * "crypto:trades" 채널 메시지를 실시간으로 스트리밍합니다.
     */
    public Flux<String> subscribeTrades() {
        return redisContainer.receive(cryptoTradesTopic)
                .map(ReactiveSubscription.Message::getMessage);
    }
}
