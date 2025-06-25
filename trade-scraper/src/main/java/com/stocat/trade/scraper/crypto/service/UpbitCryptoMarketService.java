package com.stocat.trade.scraper.crypto.service;

import com.stocat.trade.scraper.crypto.dto.response.MarketTradeDetailResponse;
import com.stocat.trade.scraper.crypto.dto.response.MarketEventDetailResponse;
import com.stocat.trade.scraper.crypto.dto.MarketInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Upbit REST API 를 호출하여
 * - 전체 종목 중 거래량 급등 이벤트(trading volume soaring) 종목을 필터링하고,
 * - 상위 N개만 MarketInfo 형태로 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class UpbitCryptoMarketService {

    private final WebClient upbitWebClient;

    public List<MarketTradeDetailResponse> getTopKrwTradeCrypto(int n) {
        return Objects.requireNonNull(upbitWebClient.get()
                        .uri(uri -> uri.path("/v1/ticker/all")
                                .queryParam("quote_currencies", "KRW")
                                .build())
                        .header(HttpHeaders.ACCEPT, "application/json")
                        .retrieve()
                        .bodyToFlux(MarketTradeDetailResponse.class)
                        .collectList()
                        .block())
                .stream()
                .sorted(Comparator.comparingDouble(MarketTradeDetailResponse::accTradeVolume24h))
                .limit(n)
                .toList();
    }

    /**
     * 전체 코인 종목 조회
     */
    public Set<MarketInfo> getAllMarkets() {
        return upbitWebClient.get()
                .uri(uri -> uri.path("/v1/market/all")
                        .queryParam("isDetails", "true")
                        .build())
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToFlux(MarketEventDetailResponse.class)
                .collectList()
                .map(this::toMarketInfo)
                .block();
    }

    private Set<MarketInfo> toMarketInfo(List<MarketEventDetailResponse> response) {
        return response.stream().map(detail -> new MarketInfo(detail.market(), detail.koreanName(), detail.englishName())).collect(Collectors.toSet());
    }

    /**
     * 1) allDetails 에서 marketEvent.caution 에 "SOARING" 포함된 market 코드만 뽑아서 셔플
     * 2) allDetails 에서 모든 market 코드만 뽑아서 셔플
     * 3) 두 스트림을 합친 뒤 distinct() -> limit 개수만큼 반환
     */
    @Deprecated
    private List<MarketEventDetailResponse> pickOrFill(Collection<MarketEventDetailResponse> allDetails, int limit) {
        List<MarketEventDetailResponse> marketList = new ArrayList<>(getVolumeSoaring(allDetails));
        Collections.shuffle(marketList);

        if (marketList.size() >= limit) {
            return marketList.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        int remaining = limit - marketList.size();
        List<MarketEventDetailResponse> otherCaution = getSoaringWithoutVolume(allDetails, marketList);
        Collections.shuffle(otherCaution);

        otherCaution = otherCaution.stream().limit(remaining).toList();
        marketList.addAll(otherCaution);

        if (marketList.size() >= limit) {
            return marketList.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        remaining = limit - marketList.size();
        List<MarketEventDetailResponse> reamainList = allDetails.stream()
                .filter(list -> !marketList.contains(list))
                .limit(remaining)
                .toList();

        marketList.addAll(reamainList);
        return marketList;
    }

    private List<MarketEventDetailResponse> getSoaringWithoutVolume(Collection<MarketEventDetailResponse> allDetails, List<MarketEventDetailResponse> volSoaring) {
        return allDetails.stream()
                .filter(md -> md.marketEvent() != null && md.marketEvent().caution() != null)
                .filter(md -> md.marketEvent().caution().entrySet().stream()
                        .anyMatch(e -> Boolean.TRUE.equals(e.getValue())))
                .filter(md -> !volSoaring.contains(md))
                .distinct()
                .toList();
    }

    private List<MarketEventDetailResponse> getVolumeSoaring(Collection<MarketEventDetailResponse> allDetails) {
        return allDetails.stream()
                .filter(md -> md.marketEvent() != null && md.marketEvent().caution() != null)
                .filter(md -> Boolean.TRUE.equals(md.marketEvent().caution().get("TRADING_VOLUME_SOARING")))
                .distinct()
                .toList();
    }

}
