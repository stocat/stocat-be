package com.stocat.crypto.websocket.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication(
        scanBasePackages = {
                "com.stocat.crypto.websocket.api",
                "com.stocat.common.redis"
        }
)
public class CryptoWebsocketApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoWebsocketApiApplication.class, args);
    }

}
