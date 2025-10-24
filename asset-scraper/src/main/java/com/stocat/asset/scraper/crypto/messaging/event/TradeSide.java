package com.stocat.asset.scraper.crypto.messaging.event;

/**
 * 매매 구분
 */
public enum TradeSide {
    BUY,
    SELL;

    public static TradeSide fromUpbitAb(String ab) {
        return "ASK".equalsIgnoreCase(ab) ? SELL : BUY;
    }
}
