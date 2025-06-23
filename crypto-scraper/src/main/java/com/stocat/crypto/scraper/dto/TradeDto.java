package com.stocat.crypto.scraper.dto;

/**
 * 체결 정보용 DTO
 */
public record TradeDto(
        String code,
        double price,
        double changePrice,
        double changeRate
) {
}