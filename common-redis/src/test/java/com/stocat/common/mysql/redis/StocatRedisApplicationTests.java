package com.stocat.common.mysql.redis;

import com.stocat.common.mysql.redis.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {RedisConfig.class})
class StocatRedisApplicationTests {

    @Test
    void contextLoads() {
    }

}
