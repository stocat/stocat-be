package com.stocat.trade.scraper.crypto.scheduler;

import com.stocat.trade.scraper.crypto.config.UpbitApiProperties;
import com.stocat.trade.scraper.crypto.dto.response.MarketTradeDetailResponse;
import com.stocat.trade.scraper.crypto.service.SubscriptionCodeService;
import com.stocat.trade.scraper.crypto.service.SymbolMappingService;
import com.stocat.trade.scraper.crypto.service.UpbitCryptoMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CryptoJob {

    private final UpbitApiProperties upbitApiProperties;

    private final SubscriptionCodeService subscriptionCodeService;
    private final UpbitCryptoMarketService upbitCryptoMarketService;
    private final SymbolMappingService symbolMappingService;

    /**
     * 매일 00:00에 스케줄
     * 1) Upbit에서 Top N개의 랜덤 종목 조회
     * 2) Upbit에서 전체 종목 정보 조회 후 매핑 정보 업데이트
     * 3) 기존 hotKey 리스트 삭제 후 PUSH
     * 4) 같은 5개 코드를 subscribeKey 에도 PUSH
     * → SubscriptionCodeService가 자동 재구독
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(fixedRate = 60_000, initialDelay = 1000) // 로컬 테스트용
    public void refreshHodCodeAndAddJobSubscribe() {
        List<MarketTradeDetailResponse> dailyCodes = upbitCryptoMarketService.getTopKrwTradeCrypto(upbitApiProperties.getTopLimit());
        upbitCryptoMarketService.getAllMarkets().forEach(symbolMappingService::upsert);
        subscriptionCodeService.refreshHotAndSubscribeCodes(dailyCodes.stream().map(MarketTradeDetailResponse::market).toList().toArray(new String[0]));
        subscriptionCodeService.reloadCodes();
    }

}
