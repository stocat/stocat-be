package com.stocat.crypto.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.stocat.crypto.scraper",
                "com.stocat.common.redis"
        }
)
public class CryptoScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoScraperApplication.class, args);
    }

}
