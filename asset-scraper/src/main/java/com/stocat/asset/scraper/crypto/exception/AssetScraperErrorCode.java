package com.stocat.asset.scraper.crypto.exception;

import com.stocat.common.mysql.exception.ErrorCode;
import com.stocat.common.mysql.exception.ErrorDomain;

public enum AssetScraperErrorCode implements ErrorCode {
    INTERNAL_ERROR(ErrorDomain.ASSET_SCRAPER.offset(), "서버 에러가 발생했습니다."),
    INVALID_UPBIT_SIDE(ErrorDomain.ASSET_SCRAPER.offset() + 1, "업비트 체결 구분 값이 올바르지 않습니다."),
    EMPTY_TARGET_SYMBOLS(ErrorDomain.ASSET_SCRAPER.offset()+2,"타깃 심볼이 비어 있어 Redis 갱신을 건너뜁니다."),
    EMPTY_TRADE_INFOS(ErrorDomain.ASSET_SCRAPER.offset()+3, "TradeInfo 이 비어 있어 publishTrades()을 건너뜁니다.")
    ;

    private final int code;
    private final String message;

    AssetScraperErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
