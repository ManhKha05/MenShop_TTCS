package com.ttcs.menshop.modules.brand.repository;

import com.ttcs.menshop.modules.brand.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<BrandEntity, Integer> {
    Optional<BrandEntity> findByNameIgnoreCase(String name);
}
