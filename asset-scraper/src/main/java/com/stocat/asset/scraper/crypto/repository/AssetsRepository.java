package com.stocat.asset.scraper.crypto.repository;

import com.stocat.asset.scraper.crypto.model.AssetsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetsRepository extends JpaRepository<AssetsEntity, Long> {
}
