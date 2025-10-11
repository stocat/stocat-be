package com.stocat.trade.scraper.repository;

import com.stocat.trade.scraper.model.AssetsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetsRepository extends JpaRepository<AssetsEntity, Long> {
    List<AssetsEntity> findByIsActiveIsTrue();
}
