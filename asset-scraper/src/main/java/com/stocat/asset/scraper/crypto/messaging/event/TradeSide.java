package com.stocat.asset.scraper.crypto.messaging.event;

/**
 * 매매 구분
 */
public enum TradeSide {
    BUY,
    SELL;

    public static TradeSide fromUpbitSide(String side) {
        if (side.equals("ASK")){
            return SELL;
        } else if (side.equals("BID")) {
            return BUY;
        }
        // TODO: 에러 핸들링
        throw new IllegalArgumentException();
    }
}
