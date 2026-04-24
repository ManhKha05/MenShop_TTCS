package com.ttcs.menshop.modules.sale_product.repository;

import com.ttcs.menshop.modules.sale_product.entity.SaleProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SaleProductRepository extends JpaRepository<SaleProductEntity, Integer> {
    @Query("""
        SELECT sp
        FROM SaleProductEntity sp
        WHERE sp.flashSale.id = :flashSaleId
        AND sp.product.id IN :productIds
    """)
    List<SaleProductEntity> findByFlashSaleAndProductIds(
            @Param("flashSaleId") Integer flashSaleId,
            @Param("productIds") List<Integer> productIds
    );

    List<SaleProductEntity> findByFlashSaleId(Integer flashSaleId);

    @Query("""
        SELECT sp
        FROM SaleProductEntity sp
        JOIN FETCH sp.product p
        JOIN FETCH p.shop
        LEFT JOIN FETCH p.images
        WHERE sp.flashSale.id = :flashSaleId
    """)
    Page<SaleProductEntity> findProductsByFlashSaleId(
            @Param("flashSaleId") Integer flashSaleId,
            Pageable pageable
    );

    @Query("""
        SELECT fsp FROM SaleProductEntity fsp
        WHERE fsp.product.id = :productId
          AND CURRENT_TIMESTAMP BETWEEN fsp.flashSale.startTime AND fsp.flashSale.endTime
    """)
    Optional<SaleProductEntity> findActiveByProductId(Integer productId);

}
