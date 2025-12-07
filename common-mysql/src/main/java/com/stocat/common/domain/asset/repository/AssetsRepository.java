package com.stocat.common.domain.asset.repository;

import com.stocat.common.domain.asset.domain.Assets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetsRepository extends JpaRepository<Assets, Long> {
}
