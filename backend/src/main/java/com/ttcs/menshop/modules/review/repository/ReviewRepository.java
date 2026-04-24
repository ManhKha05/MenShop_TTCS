package com.ttcs.menshop.modules.review.repository;

import com.ttcs.menshop.modules.review.dto.response.ReviewResponse;
import com.ttcs.menshop.modules.review.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    @Query("""
        select distinct r
        from ReviewEntity r
        join fetch r.user
        left join fetch r.images
        where r.product.id = :productId
        order by r.createdAt desc
    """)
    Page<ReviewEntity> findByProductId(@Param("productId") Integer productId, Pageable pageable);

    @Query("""
        select avg(r.rating) from ReviewEntity r where r.product.id = :productId
    """)
    BigDecimal avgRatingByProductId(@Param("productId") Integer productId);

//    @Query("""
//        select count(r) from ReviewEntity r where r.product.id = :productId
//    """)
//    Long countByProductId(@Param("productId") Integer productId);

    boolean existsByUserIdAndProductIdAndOrderId(Integer userId, Integer productId, Integer orderId);

    @Query("""
        select distinct r
        from ReviewEntity r
        join r.product p
        join p.shop s
        where s.id = :shopId
        and (:keyword is null or p.name like concat('%', :keyword, '%'))
        and (
            :rating is null
            or ( :rating = 2 and r.rating <= 2 )
            or r.rating = :rating
        )
        order by
            case when :sort = 'rating' then r.rating end desc,
            r.createdAt desc
    """)
    Page<ReviewEntity> findByShop(
            @Param("shopId") Integer shopId,
            @Param("rating") Integer rating,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            Pageable pageable
    );
}
