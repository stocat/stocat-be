package com.stocat.trade.scraper.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("upbit.api")
@Getter
@Setter
public class UpbitApiProperties {
    private String restBaseUrl;
    private String wsUrl;
    private int topLimit;
}
