package com.ttcs.menshop.modules.shop.repository;

import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<ShopEntity, Integer> {
    Optional<ShopEntity> findByUserId(Integer userId);

    @Query("""
        SELECT s FROM ShopEntity s 
        WHERE (:keyword IS NULL OR s.name LIKE CONCAT('%', :keyword, '%'))
            AND (:status IS NULL OR s.status = :status)
    """)
    Page<ShopEntity> search(String keyword, String status, Pageable pageable);

    Long countByStatus(String status);

    List<ShopEntity> findTop5ByOrderByCreatedAtDesc();
}
