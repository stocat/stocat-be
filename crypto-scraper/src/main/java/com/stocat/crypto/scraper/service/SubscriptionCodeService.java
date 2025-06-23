package com.stocat.crypto.scraper.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

/**
 * Redis의 "crypto:subscribe_codes" 리스트를 하루에 한 번 읽어와서
 * 변경이 감지될 때마다 Flux로 흘려줍니다.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionCodeService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final Sinks.Many<List<String>> sink = Sinks.many().replay().latest();

    @Value("${subscription.redis.key:crypto:subscribe_codes}")
    private String redisKey;

    /**
     * 애플리케이션 시작 시 한 번 구독 리스트를 로드합니다.
     */
    @PostConstruct
    public void init() {
        reloadCodes();
    }

    /**
     * 매일 00:00에 Redis에서 리스트를 다시 읽어 와서 구독 Flux에 푸시합니다.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void reloadCodes() {
        redisTemplate.opsForList()
                .range(redisKey, 0, -1)
                .collectList()
                .filter(list -> !list.isEmpty())
                .doOnNext(sink::tryEmitNext)
                .subscribe();
    }

    /**
     * 구독 코드가 로드될 때마다 리스트를 방출하는 Flux.
     * UpbitWebSocketScraperService 에서 이 Flux를 구독하면,
     * 새로운 리스트가 나올 때마다 재접속 로직으로 이어집니다.
     */
    public Flux<List<String>> codeFlux() {
        return sink.asFlux();
    }
}