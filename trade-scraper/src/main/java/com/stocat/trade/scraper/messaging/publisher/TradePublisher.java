package com.stocat.trade.scraper.messaging.publisher;

import com.stocat.trade.scraper.service.SubscriptionCodeService;
import com.stocat.trade.scraper.service.UpbitCryptoScrapeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradePublisher {

    private final SubscriptionCodeService subscriptionCodeService;
    private final UpbitCryptoScrapeService upbitCryptoScrapeService;

    @PostConstruct
    public void start() {
        // reloadCodes() 호출로 구독 키 리스트가 갱신될 때마다 websocket 다시 열고, 체결 데이터 redis 채널로 pub 파이프라인
        subscriptionCodeService.codeFlux()
                .switchMap(upbitCryptoScrapeService::streamTrades)
                .flatMap(subscriptionCodeService::publishTrades)
                .subscribe(
                        null,
                        err -> System.err.println("WebSocket 오류: " + err.getMessage())
                );
    }

}
