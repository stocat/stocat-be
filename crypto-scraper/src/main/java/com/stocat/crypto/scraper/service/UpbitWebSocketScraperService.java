package com.stocat.crypto.scraper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocat.crypto.scraper.dto.TradeDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static com.stocat.common.redis.constants.CryptoKeys.CRYPTO_TRADES;

/**
 * SubscriptionCodeService.codeFlux()에서 전달된 종목 리스트마다
 * Upbit WS에 재구독하고, 수신된 체결 메시지를 Redis "crypto:trades" 채널에 퍼블리시합니다.
 */
@Service
@RequiredArgsConstructor
public class UpbitWebSocketScraperService {
    private final SubscriptionCodeService codes;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ReactorNettyWebSocketClient wsClient = new ReactorNettyWebSocketClient();

    @Value("${UPBIT_WS_URL:wss://api.upbit.com/websocket/v1}")
    private String wsUrl;

    @PostConstruct
    public void start() {
        codes.codeFlux()
                .switchMap(this::connectAndPublish)
                .subscribe(
                        null,
                        err -> System.err.println("WebSocket 오류: " + err.getMessage())
                );
    }

    /**
     * 주어진 종목 리스트로 WS 연결 → 메시지 처리 → Redis 퍼블리시
     */
    private Mono<Void> connectAndPublish(List<String> symbols) {
        String payload = buildSubscribePayload(symbols);
        return wsClient.execute(
                URI.create(wsUrl),
                session -> session
                        .send(Mono.just(session.textMessage(payload)))
                        .thenMany(session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .flatMap(this::parseJson)
                                .filter(this::isTrade)
                                .map(this::toTradeDto)
                                .flatMap(this::publishToRedis)
                        )
                        .then()
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
                // 파싱 실패 시 빈값으로 폴링아웃
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
    private TradeDto toTradeDto(JsonNode node) {
        double price = node.path("tp").asDouble();
        double prevClose = node.path("pcp").asDouble();
        double change = price - prevClose;
        double changeRate = change / prevClose;
        return new TradeDto(
                node.path("cd").asText(),
                price,
                change,
                changeRate
        );
    }

    /**
     * TradeDto를 JSON으로 직렬화하여 Redis 채널에 발행합니다.
     *
     * @return 퍼블리시 후 구독자 수
     */
    private Mono<Long> publishToRedis(TradeDto dto) {
        try {
            String json = mapper.writeValueAsString(dto);
            return redisTemplate.convertAndSend(CRYPTO_TRADES, json);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

}