package com.stocat.stock.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Redis 연결 및 Pub/Sub 설정을 담당합니다.
 */
@Configuration
public class RedisConfig {
    /**
     * Redis 연결 팩토리 생성
     */
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    /**
     * Pub/Sub용 Redis 템플릿 생성
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory, RedisSerializationContext.string());
    }
}
