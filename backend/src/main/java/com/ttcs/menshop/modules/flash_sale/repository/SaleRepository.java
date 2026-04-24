package com.ttcs.menshop.modules.flash_sale.repository;

import com.ttcs.menshop.modules.flash_sale.entity.SaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<SaleEntity, Integer> {

    @Query("SELECT COUNT(s) FROM SaleEntity s WHERE s.isDisabled = false")
    long countTotal();

    @Query("""
        SELECT COUNT(s) FROM SaleEntity s
        WHERE s.isDisabled = false
        AND NOW() BETWEEN s.startTime AND s.endTime
    """)
    long countActive();

    @Query("""
        SELECT COUNT(s) FROM SaleEntity s
        WHERE s.isDisabled = false
        AND NOW() < s.startTime
    """)
    long countUpcoming();

    @Query("""
        SELECT COUNT(s) FROM SaleEntity s
        WHERE s.isDisabled = false
        AND NOW() >= s.endTime
    """)
    long countEnded();

    @Query("""
        SELECT s FROM SaleEntity s
        WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (s.isDisabled = false)
        AND ( (:status IS NULL)
            OR(:status = 'ACTIVE' AND s.isDisabled = false AND s.startTime <= NOW() AND s.endTime >= NOW())
            OR (:status = 'UPCOMING' AND s.isDisabled = false AND s.startTime > NOW())
            OR (:status = 'ENDED' AND s.isDisabled = false AND s.endTime < NOW())
            OR (:status = 'DISABLED' AND s.isDisabled = true)
            )
    """)
    Page<SaleEntity> search(String keyword, String status, Pageable pageable);

    @Query("""
        SELECT s FROM SaleEntity s
        WHERE s.isDisabled = false
        AND s.endTime > NOW()
        AND (:start < s.endTime AND :end > s.startTime)
        ORDER BY s.endTime ASC
        LIMIT 1
    """)
    SaleEntity findOverlapping(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT fs
        FROM SaleEntity fs
        WHERE fs.isDisabled = false
          AND :now BETWEEN fs.startTime AND fs.endTime
    """)
    SaleEntity findActiveFlashSale(LocalDateTime now);

}
