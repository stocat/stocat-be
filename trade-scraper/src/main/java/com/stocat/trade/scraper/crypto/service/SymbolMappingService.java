package com.stocat.trade.scraper.crypto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocat.common.redis.constants.CryptoKeys;
import com.stocat.trade.scraper.crypto.dto.MarketInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SymbolMappingService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;

    private HashOperations<String, String, String> hashOps() {
        return redisTemplate.opsForHash();
    }

    /**
     * 생성 or 갱신
     */
    public void upsert(MarketInfo info) {
        try {
            String json = mapper.writeValueAsString(info);
            hashOps().put(CryptoKeys.CRYPTO_SYMBOL_MAPPINGS, info.code(), json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize MarketInfo", e);
        }
    }

}
