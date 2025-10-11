package com.stocat.trade.scraper.messaging.event;

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
