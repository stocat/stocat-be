package com.stocat.trade.scraper.crypto.messaging.publisher;

import com.stocat.trade.scraper.crypto.service.SubscriptionCodeService;
import com.stocat.trade.scraper.crypto.service.UpbitCryptoScrapeService;
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
