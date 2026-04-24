package com.ttcs.menshop.modules.product.repository;

import com.ttcs.menshop.modules.product.dto.response.ProductCardResponse;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {

    @Query("""
        SELECT 
            COUNT(p),
            SUM(CASE WHEN p.status = 'PENDING' THEN 1 ELSE 0 END),
            SUM(CASE WHEN p.status = 'ACTIVE' THEN 1 ELSE 0 END),
            SUM(CASE WHEN p.status = 'INACTIVE' THEN 1 ELSE 0 END),
            SUM(CASE WHEN p.status = 'ACTIVE' 
                     AND NOT EXISTS (
                         SELECT 1 FROM ProductVariantEntity pv 
                         WHERE pv.product = p 
                         AND pv.stock > 0
                     ) THEN 1 ELSE 0 END)
        FROM ProductEntity p
        WHERE (:shopId IS NULL OR p.shop.id = :shopId)
    """)
    List<Object[]> getProductStats(Integer shopId);

    @Query("""
        SELECT p FROM ProductEntity p
        WHERE (:shopId IS NULL OR p.shop.id = :shopId)
            AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (:status IS NULL OR p.status = :status)
    """)
    Page<ProductEntity> search(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("status") String status,
            Integer shopId,
            Pageable pageable
    );

    @Query("""
        SELECT p
        FROM ProductEntity p
        WHERE p.shop.id = :shopId
        AND p.status = 'ACTIVE'
        AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<ProductEntity> getProductsForFlashSale(
            @Param("shopId") Integer shopId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(p)
        FROM ProductEntity p
        WHERE p.shop.id = :shopId
          AND p.status = 'ACTIVE'
    """)
    long countActiveProductsByShopId(@Param("shopId") Integer shopId);

    @Query("""
        SELECT p
        FROM ProductEntity p
        WHERE p.shop.id = :shopId
          AND p.status = 'ACTIVE'
        ORDER BY p.soldCount DESC
    """)
    List<ProductEntity> findBestSellingProductsByShop(@Param("shopId") Integer shopId, Pageable pageable);

    @Query(value = """
        SELECT p.*
        FROM product p
        LEFT JOIN flash_sale_product fsp
            ON fsp.product_id = p.id
        LEFT JOIN flash_sale fs
            ON fs.id = fsp.flash_sale_id
            AND NOW() BETWEEN fs.start_time AND fs.end_time
            AND fs.is_disabled = FALSE
        WHERE p.shop_id = :shopId
        AND (:category IS NULL OR p.category_id = :category)
        AND p.status = 'ACTIVE'
        ORDER BY
            CASE WHEN :priceOrder = 'asc' THEN COALESCE(fsp.sale_price, p.sale_price, p.price) END ASC,
            CASE WHEN :priceOrder = 'desc' THEN COALESCE(fsp.sale_price, p.sale_price, p.price) END DESC,
            CASE WHEN :priceOrder IS NULL THEN
                CASE
                    WHEN :sort = 'newest' THEN p.created_at
                    WHEN :sort = 'best-selling' THEN p.sold_count
                    ELSE p.rating_avg
                END
            END DESC
        """, nativeQuery = true)
    Page<ProductEntity> getProductsForShop(
            @Param("shopId") Integer shopId,
            @Param("category") Integer category,
            @Param("sort") String sort,
            @Param("priceOrder") String priceOrder,
            Pageable pageable
    );

    List<ProductEntity> findTop5ByStatusOrderByCreatedAtDesc(String status);
    @Query("""
        select count(p)
        from ProductEntity p
        where p.status = 'ACTIVE'
          and p.category.id in :categoryIds
    """)
    Long countActiveByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);

    @Query("""
    select new com.ttcs.menshop.modules.product.dto.response.ProductCardResponse(
        p.id,
        p.name,
        (
            select min(img.imageUrl)
            from ProductImageEntity img
            where img.product.id = p.id
        ),
        p.price,
        p.salePrice,
        (
            select fsp.salePrice
            from SaleProductEntity fsp
            join fsp.flashSale fs
            where fsp.product.id = p.id
              and fs.isDisabled = false
              and current_timestamp between fs.startTime and fs.endTime
        ),
        s.name,
        p.ratingAvg,
        p.soldCount
    )
    from ProductEntity p
    join p.shop s
    where p.status = :status
      and p.category.id in :categoryIds
      and (:minPrice is null or coalesce(
            (
                select min(fsp.salePrice)
                from SaleProductEntity fsp
                join fsp.flashSale fs
                where fsp.product.id = p.id
                  and fs.isDisabled = false
                  and current_timestamp between fs.startTime and fs.endTime
            ),
            p.salePrice,
            p.price
      ) >= :minPrice)
      and (:maxPrice is null or coalesce(
            (
                select min(fsp.salePrice)
                from SaleProductEntity fsp
                join fsp.flashSale fs
                where fsp.product.id = p.id
                  and fs.isDisabled = false
                  and current_timestamp between fs.startTime and fs.endTime
            ),
            p.salePrice,
            p.price
      ) <= :maxPrice)
      and (:rating is null or p.ratingAvg >= :rating)
""")
    Page<ProductCardResponse> searchByCategoryIds(
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("status") String status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("rating") Integer rating,
            Pageable pageable
    );
}
