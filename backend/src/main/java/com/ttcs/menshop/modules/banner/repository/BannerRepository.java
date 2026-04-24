package com.ttcs.menshop.modules.banner.repository;

import com.ttcs.menshop.modules.banner.entity.BannerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity, Integer> {

    List<BannerEntity> findByStatus(String status);

    @Query("""
        SELECT b FROM BannerEntity b
        WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:status IS NULL OR b.status = :status)
    """)
    Page<BannerEntity> search(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

    Long countByStatus(String status);
}
