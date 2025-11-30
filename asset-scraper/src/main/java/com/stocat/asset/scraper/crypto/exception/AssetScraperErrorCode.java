package com.stocat.asset.scraper.crypto.exception;

import com.stocat.common.exception.ErrorCode;
import com.stocat.common.exception.ErrorDomain;

public enum AssetScraperErrorCode implements ErrorCode {
    INTERNAL_ERROR(ErrorDomain.ASSET_SCRAPER.offset(), "서버 에러가 발생했습니다.")
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