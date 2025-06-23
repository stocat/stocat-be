package com.stocat.stock.scraper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TickStreamSubscriber {

    private final WebClient authClient;
    private final ReactorNettyWebSocketClient wsClient;
    private final RedisTradePublisher publisher;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${kis.client.id}")
    private String clientId;
    @Value("${kis.client.secret}")
    private String clientSecret;
    @Value("${kis.websocket-url}")
    private String websocketUrl;
    @Value("${kis.auth-base-url}")
    private String authBaseUrl;

    // 구독할 종목 리스트
    private final List<String> symbols = List.of("005930", "000660");

    public TickStreamSubscriber(
            RedisTradePublisher publisher,
            WebClient.Builder webClientBuilder
    ) {
        this.publisher = publisher;
        this.authClient = webClientBuilder
                .baseUrl("https://openapivts.koreainvestment.com:29443")
                .build();
        this.wsClient = new ReactorNettyWebSocketClient();
    }

    /**
     * 애플리케이션 시작 시 호출됩니다.
     * 1) OAuth2 Approval을 통해 approvalKey를 발급받고,
     * 2) WebSocket에 연결하여 실시간 체결 메시지를 스트리밍 처리합니다.
     */
//    @PostConstruct
    public void init() {

        System.out.println(authBaseUrl + " subscribed to tick stream");
        System.out.println(websocketUrl);
        authClient.post()
                .uri("/oauth2/Approval")
                .bodyValue(new ApprovalRequest("client_credentials", clientId, clientSecret))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.get("approval_key").asText())
                .flatMap(this::streamAndProcess)
                .subscribe();  // 엔트리 포인트에서만 subscribe 호출
    }

    /**
     * approvalKey로 WebSocket에 연결하여 체결 데이터를 수신하고,
     * processAndPublish()를 통해 Redis Pub/Sub로 발행합니다.
     */
    private Mono<Void> streamAndProcess(String approvalKey) {
        Map<String, Object> request = getStringObjectMap(approvalKey);

        String subscribeMsg = "";
        try {
            subscribeMsg = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String finalSubscribeMsg = subscribeMsg;
        return wsClient.execute(
                URI.create(websocketUrl),
                session -> session.send(
                                Flux.just(session.textMessage(finalSubscribeMsg))
                        )
                        .thenMany(
                                session.receive()
                                        .map(WebSocketMessage::getPayloadAsText)
                                        .flatMap(this::processAndPublish)
                        )
                        .then()
        );
    }

    private Map<String, Object> getStringObjectMap(String approvalKey, String trId, String trKey) {
        Map<String, Object> header = new HashMap<>();
        header.put("approval_key", approvalKey);
        header.put("tr_type", "1");
        header.put("custtype", "P");
        header.put("content-type", "utf-8");

        Map<String, String> input = new HashMap<>();
        input.put("tr_id", trId);
        input.put("tr_key", trKey);

        Map<String, Object> body = new HashMap<>();
        body.put("input", input);

        Map<String, Object> request = new HashMap<>();
        request.put("header", header);
        request.put("body", body);
        return request;
    }

    /**
     * JSON 메시지를 파싱하여 지정 종목인 경우 Redis Pub/Sub로 발행합니다.
     */
    private Mono<Void> processAndPublish(String jsonText) {
        try {
            JsonNode node = mapper.readTree(jsonText);
            String code = node.at("/body/output/stock_code").asText();
            if (symbols.contains(code)) {
                BigDecimal price = new BigDecimal(node.at("/body/output/price").asText());
                return publisher.publishTrade(code, price);
            }
        } catch (Exception e) {
            // 파싱 오류 로깅
            e.printStackTrace();
        }
        return Mono.empty();
    }

    private record ApprovalRequest(String grant_type, String appkey, String secretkey) {}
}
