package com.stocat.stock.scraper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 실시간 체결 데이터를 Redis에 키-값 형태로 저장합니다.
 */
@Service
@RequiredArgsConstructor
public class RedisTradePublisher {
    private final ReactiveStringRedisTemplate redis;
    private final ChannelTopic topic = new ChannelTopic("stock:trades");

    /**
     * 지정 종목의 체결가(가격)를 Pub/Sub 채널에 발행합니다.
     * @param symbol 종목 코드
     * @param price  체결 가격
     * @return 발행 완료 시 Mono<Void>
     */
    public Mono<Void> publishTrade(String symbol, BigDecimal price) {
        String payload = String.format(
                "{\"symbol\":\"%s\",\"price\":%s,\"ts\":\"%s\"}",
                symbol, price, Instant.now().toString()
        );
        return redis.convertAndSend(topic.getTopic(), payload).then();
    }
}
