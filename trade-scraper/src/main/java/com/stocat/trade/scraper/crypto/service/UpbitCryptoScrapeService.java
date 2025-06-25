package com.stocat.trade.scraper.crypto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocat.trade.scraper.crypto.config.UpbitApiProperties;
import com.stocat.trade.scraper.crypto.messaging.event.TradeInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;

/**
 * SubscriptionCodeService.codeFlux()에서 전달된 종목 리스트마다
 * Upbit WS에 재구독하고, 수신된 체결 메시지를 Redis "crypto:trades" 채널에 퍼블리시합니다.
 */
@Service
@RequiredArgsConstructor
public class UpbitCryptoScrapeService {
    private final ObjectMapper mapper;
    private final ReactorNettyWebSocketClient webSocketClient;
    private final UpbitApiProperties upbitApiProperties;

    /**
     * 주어진 심볼 리스트로 WebSocket에 연결하여
     * 단순히 TradeDto 파이프라인만 반환합니다.
     */
    public Flux<TradeInfo> streamTrades(List<String> symbols) {
        String payload = buildSubscribePayload(symbols);

        return Flux.create(sink ->
                webSocketClient.execute(
                                URI.create(upbitApiProperties.getWsUrl()),
                                session -> session
                                        .send(Mono.just(session.textMessage(payload)))
                                        .thenMany(
                                                session.receive()
                                                        .map(WebSocketMessage::getPayloadAsText)
                                                        .flatMap(this::parseJson)
                                                        .filter(this::isTrade)
                                                        .map(this::toTradeDto)
                                                        .doOnNext(sink::next)
                                        )
                                        .then()
                        )
                        .doOnError(sink::error)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe()
        );
    }

    /**
     * 구독 페이로드(JSON Array)를 생성합니다.
     */
    private String buildSubscribePayload(List<String> symbols) {
        var ticket = mapper.createObjectNode()
                .put("ticket", "daily-" + System.currentTimeMillis());
        var type = mapper.createObjectNode()
                .put("type", "trade")
                .set("codes", mapper.valueToTree(symbols));
        var format = mapper.createObjectNode()
                .put("format", "SIMPLE");
        return mapper.createArrayNode()
                .add(ticket)
                .add(type)
                .add(format)
                .toString();
    }

    /**
     * 원시 JSON 문자열을 JsonNode로 파싱합니다.
     */
    private Mono<JsonNode> parseJson(String raw) {
        return Mono.fromCallable(() -> mapper.readTree(raw))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * SIMPLE 포맷 메시지인지, 그리고 체결(trade) 타입인지 확인합니다.
     */
    private boolean isTrade(JsonNode node) {
        return "trade".equals(node.path("ty").asText());
    }

    /**
     * JsonNode를 TradeDto로 변환(체결가, 등락가, 등락률 계산 포함).
     */
    private TradeInfo toTradeDto(JsonNode node) {
        double price = node.path("tp").asDouble();
        double prevClose = node.path("pcp").asDouble();
        double change = price - prevClose;
        double changeRate = change / prevClose;
        return new TradeInfo(
                node.path("cd").asText(),
                price,
                change,
                changeRate
        );
    }
}