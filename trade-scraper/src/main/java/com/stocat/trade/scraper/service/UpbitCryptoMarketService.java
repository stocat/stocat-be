package com.stocat.trade.scraper.service;

import com.stocat.trade.scraper.dto.response.MarketDetailResponse;
import com.stocat.trade.scraper.dto.MarketInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Upbit REST API 를 호출하여
 * - 전체 종목 중 거래량 급등 이벤트(trading volume soaring) 종목을 필터링하고,
 * - 상위 N개만 MarketInfo 형태로 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class UpbitCryptoMarketService {

    private final WebClient upbitWebClient;

    /**
     * SOARING 이벤트 종목 중 랜덤 샘플 n 개를 반환합니다.
     * 만약 SOARING 종목이 n개 미만이라면, 전체 코드에서 채워 넣습니다.
     */
    public Set<MarketInfo> getDailyCrypto(int n) {
        return upbitWebClient.get()
                .uri(uri -> uri.path("/v1/market/all")
                        .queryParam("isDetails", "true")
                        .build())
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToFlux(MarketDetailResponse.class)
                .collectList()
                .map(list -> pickOrFill(list, n))
                .map(this::toMarketInfo)
                .block();


    }

    private Set<MarketInfo> toMarketInfo(List<MarketDetailResponse> marketDetailResponses) {
        return marketDetailResponses.stream().map(detail -> new MarketInfo(detail.market(), detail.koreanName(), detail.englishName())).collect(Collectors.toSet());
    }

    /**
     * 1) allDetails 에서 marketEvent.caution 에 "SOARING" 포함된 market 코드만 뽑아서 셔플
     * 2) allDetails 에서 모든 market 코드만 뽑아서 셔플
     * 3) 두 스트림을 합친 뒤 distinct() -> limit 개수만큼 반환
     */
    private List<MarketDetailResponse> pickOrFill(List<MarketDetailResponse> allDetails, int limit) {
        List<MarketDetailResponse> soaring = allDetails.stream()
                .filter(md -> md.marketEvent() != null && md.marketEvent().caution() != null)
                .filter(md -> md.marketEvent().caution().entrySet()
                        .stream().anyMatch(entry -> entry.getValue() != null && entry.getValue()))
                .distinct()
                .collect(Collectors.toList());

        List<MarketDetailResponse> allCodes = allDetails.stream()
                .distinct()
                .collect(Collectors.toList());

        Collections.shuffle(soaring);
        Collections.shuffle(allCodes);

        // SOARING 우선, 부족하면 전체에서 채워서 limit 개수 리턴
        return Stream.concat(soaring.stream(), allCodes.stream())
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }


}
