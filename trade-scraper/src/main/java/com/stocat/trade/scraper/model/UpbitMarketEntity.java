package com.stocat.trade.scraper.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "upbit_market_metadata")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitMarketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String market;

    @Column(name = "korean_name", nullable = false, length = 100)
    private String koreanName;

    @Column(name = "english_name", nullable = false, length = 100)
    private String englishName;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public void updateMetadata(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }
}
