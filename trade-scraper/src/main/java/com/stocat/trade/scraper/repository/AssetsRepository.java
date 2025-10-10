package com.stocat.trade.scraper.repository;

import com.stocat.trade.scraper.model.AssetsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetsRepository extends JpaRepository<AssetsEntity, Long> {
}
