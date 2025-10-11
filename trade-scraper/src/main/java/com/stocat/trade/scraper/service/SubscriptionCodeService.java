package com.stocat.trade.scraper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocat.common.redis.constants.CryptoKeys;
import com.stocat.trade.scraper.dto.MarketInfo;
import com.stocat.trade.scraper.messaging.event.TradeInfo;
import com.stocat.trade.scraper.model.AssetsEntity;
import com.stocat.trade.scraper.model.enums.AssetsCategory;
import com.stocat.trade.scraper.model.enums.Currency;
import com.stocat.trade.scraper.repository.AssetsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Set;

/**
 * Redis의 "crypto:subscribe_codes" 리스트를 하루에 한 번 읽어와서
 * 변경이 감지될 때마다 Flux로 흘려줍니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCodeService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final AssetsRepository assetsRepository;
    private final ObjectMapper mapper;

    private final Sinks.Many<List<String>> sink = Sinks.many().replay().latest();

    /**
     * 애플리케이션 시작 시 한 번 구독 리스트를 로드합니다.
     */
    @PostConstruct
    public void init() {
        reloadCodes();
    }

    /**
     * Redis에서 리스트를 다시 읽어 와서 구독 Flux에 푸시합니다.
     */
    public void reloadCodes() {
        redisTemplate.opsForSet()
                .members(CryptoKeys.CRYPTO_SUBSCRIBE_CODES)
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

    /**
     * 0) DB에 기존 symbol들 isActive를 false로 update, 새 심볼 정보 insert
     * 1) 기존 hotKey 리스트 삭제 후 RPUSH
     * 2) 같은 5개 코드를 subscription.redisKey에도 RPUSH
     * → SubscriptionCodeService가 자동 재구독
     */
    @Transactional
    public void refreshHotAndSubscribeCodes(Set<MarketInfo> targetSymbols) {
        assetsRepository.findByIsActiveIsTrue()
                .forEach(AssetsEntity::deactivate);

        List<AssetsEntity> newAssets = targetSymbols.stream()
                .map(info -> AssetsEntity.builder()
                        .symbol(info.code())
                        .koName(info.koreanName())
                        .usName(info.englishName())
                        .category(AssetsCategory.CRYPTO)
                        .currency(Currency.KRW)
                        .isActive(true)
                        .build())
                .toList();
        assetsRepository.saveAll(newAssets);

        String[] codes = targetSymbols.stream()
                .map(MarketInfo::code)
                .toArray(String[]::new);

        redisTemplate.delete(CryptoKeys.CRYPTO_HOT_CODES)
                .then(redisTemplate.opsForSet().add(CryptoKeys.CRYPTO_HOT_CODES, codes))
                .then(redisTemplate.opsForSet().add(CryptoKeys.CRYPTO_SUBSCRIBE_CODES, codes))
                .subscribe(count -> System.out.println("Hot codes updated (" + count + " entries pushed)")
                        , err -> System.err.println("Failed to update hot codes: " + err.getMessage()))
        ;
    }

    /**
     * TradeDto를 JSON으로 직렬화하여 Redis 채널에 발행합니다.
     *
     * @return 퍼블리시 후 구독자 수
     */
    public Mono<Long> publishTrades(TradeInfo dto) {
        try {
            String json = mapper.writeValueAsString(dto);
            return redisTemplate.convertAndSend(CryptoKeys.CRYPTO_TRADES, json);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}