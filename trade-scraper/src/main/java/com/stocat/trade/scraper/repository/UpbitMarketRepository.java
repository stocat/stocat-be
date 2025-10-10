package com.stocat.trade.scraper.repository;

import com.stocat.trade.scraper.model.UpbitMarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpbitMarketRepository extends JpaRepository<UpbitMarketEntity, Long> {
}
