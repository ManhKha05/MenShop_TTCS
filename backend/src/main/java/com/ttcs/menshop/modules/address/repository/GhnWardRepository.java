package com.ttcs.menshop.modules.address.repository;

import com.ttcs.menshop.modules.address.entity.GhnWardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GhnWardRepository extends JpaRepository<GhnWardEntity, String> {
    List<GhnWardEntity> findByDistrictIdOrderByWardNameAsc(Integer districtId);
}