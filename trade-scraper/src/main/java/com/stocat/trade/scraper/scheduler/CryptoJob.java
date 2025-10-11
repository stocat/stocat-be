package com.stocat.trade.scraper.scheduler;

import com.stocat.trade.scraper.config.UpbitApiProperties;
import com.stocat.trade.scraper.dto.MarketInfo;
import com.stocat.trade.scraper.service.SubscriptionCodeService;
import com.stocat.trade.scraper.service.UpbitCryptoMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CryptoJob {

    private final SubscriptionCodeService subscriptionCodeService;
    private final UpbitCryptoMarketService upbitCryptoMarketService;
    private final UpbitApiProperties upbitApiProperties;


    /**
     * 매일 00:00에 스케줄
     * 1) Upbit에서 Top N개의 랜덤 종목 조회
     * 2) 기존 hotKey 리스트 삭제 후 PUSH
     * 3) 같은 5개 코드를 subscribeKey 에도 PUSH
     * → SubscriptionCodeService가 자동 재구독
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(fixedRate = 60_000, initialDelay = 1000) // 로컬 테스트용
    public void refreshHodCodeAndAddJobSubscribe() {
        Set<MarketInfo> dailyCodesSet = upbitCryptoMarketService.getTopKrwTradeCrypto(upbitApiProperties.getTopLimit());
        subscriptionCodeService.refreshHotAndSubscribeCodes(dailyCodesSet);
        subscriptionCodeService.reloadCodes();
    }

}
