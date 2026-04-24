package com.ttcs.menshop.modules.order_item.repository;

import com.ttcs.menshop.modules.order_item.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Integer> {
    List<OrderItemEntity> findByOrderId(Integer orderId);
    boolean existsByVariantId(Integer id);

    @Query("""
        select count(oi) > 0
        from OrderItemEntity oi
        join oi.variant v
        join v.product p
        where oi.order.id = :orderId
          and p.id = :productId
    """)
    boolean existsProductInOrder(@Param("orderId") Integer orderId,
                                 @Param("productId") Integer productId);
}
