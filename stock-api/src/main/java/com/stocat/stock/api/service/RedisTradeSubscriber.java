package com.stocat.stock.api.service;

import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component

public class RedisTradeSubscriber {

    private final Flux<String> updates;

    public RedisTradeSubscriber(ReactiveStringRedisTemplate redis) {
        this.updates = redis.listenTo(PatternTopic.of("stock:trades"))
                .map(ReactiveSubscription.Message::getMessage);
    }

    /**
     * 구독 중인 체결 데이터 스트림을 반환합니다.
     * @return JSON 문자열 Flux
     */
    public Flux<String> priceFlux() {
        return updates;
    }



}
