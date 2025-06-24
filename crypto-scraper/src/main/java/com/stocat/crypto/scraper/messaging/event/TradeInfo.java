package com.stocat.crypto.scraper.messaging.event;

/**
 * 체결 정보용 DTO
 */
public record TradeInfo(
        String code,
        double price,
        double changePrice,
        double changeRate
) {
}