package com.stocat.trade.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.stocat.trade.scraper",
                "com.stocat.common.redis",
                "com.stocat.common.mysql"
        }
)
public class TradeScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeScraperApplication.class, args);
    }

}
