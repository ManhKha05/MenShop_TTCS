package com.ttcs.menshop.modules.address.repository;

import com.ttcs.menshop.modules.address.entity.GhnProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GhnProvinceRepository extends JpaRepository<GhnProvinceEntity, Integer> {
    List<GhnProvinceEntity> findAllByOrderByProvinceNameAsc();
}
