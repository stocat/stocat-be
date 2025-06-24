package com.stocat.crypto.scraper.messaging.publisher;

import com.stocat.crypto.scraper.service.SubscriptionCodeService;
import com.stocat.crypto.scraper.service.UpbitCryptoScrapeService;
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
        subscriptionCodeService.codeFlux()
                .switchMap(upbitCryptoScrapeService::streamTrades)
                .flatMap(subscriptionCodeService::publishTrades)
                .subscribe(
                        null,
                        err -> System.err.println("WebSocket 오류: " + err.getMessage())
                );
    }

}
