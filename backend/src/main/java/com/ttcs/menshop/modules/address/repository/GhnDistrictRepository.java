package com.ttcs.menshop.modules.address.repository;

import com.ttcs.menshop.modules.address.entity.GhnDistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GhnDistrictRepository extends JpaRepository<GhnDistrictEntity, Integer> {
    List<GhnDistrictEntity> findByProvinceIdOrderByDistrictNameAsc(Integer provinceId);
}