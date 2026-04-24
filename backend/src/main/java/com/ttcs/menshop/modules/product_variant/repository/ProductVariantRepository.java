package com.ttcs.menshop.modules.product_variant.repository;

import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, Integer> {
    @Query("""
        SELECT v
        FROM ProductVariantEntity v
        WHERE v.product.id IN :productIds
    """)
    List<ProductVariantEntity> findByProductIds(@Param("productIds") List<Integer> productIds);

    @EntityGraph(attributePaths = {("product")})
    Optional<ProductVariantEntity> findById(Integer id);
}
