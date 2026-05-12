package com.ttcs.menshop.modules.order.repository;

import com.ttcs.menshop.modules.order.dto.response.OrderStatsResponse;
import com.ttcs.menshop.modules.order.dto.response.ShopOrderResponse;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {
    Optional<OrderEntity> findByCode(String code);
    List<OrderEntity> findByPaymentGroupId(Integer paymentGroupId);

    @Query("""
        select distinct o from OrderEntity o
        join fetch o.shop s
        left join fetch o.orderItems od
        left join fetch od.variant pv
        left join fetch pv.product p
        where o.user.id = :userId
          and (:status is null or o.status = :status)
          and (:keyword is null or lower(o.code) like lower(concat('%', :keyword, '%')))
        order by o.createdAt desc
    """)
    List<OrderEntity> findMyOrders(
            @Param("userId") Integer userId,
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    @Query("""
        select new com.ttcs.menshop.modules.order.dto.response.OrderStatsResponse(
            count(o),
            sum(case when o.status = 'PENDING' then 1 else 0 end),
            sum(case when o.status = 'CONFIRMED' then 1 else 0 end),
            sum(case when o.status = 'DELIVERING' then 1 else 0 end),
            sum(case when o.status = 'DELIVERED' then 1 else 0 end),
            sum(case when o.status = 'CANCELLED' then 1 else 0 end)
        )
        from OrderEntity o
        where o.shop.id = :shopId
    """)
    OrderStatsResponse getOrderStats(Integer shopId);

    @Query("""
        select new com.ttcs.menshop.modules.order.dto.response.ShopOrderResponse(
            o.id,
            o.code,
            o.receiverName,
            o.receiverPhone,
            o.createdAt,
            o.status,
            o.shippingStatus,
            o.paymentMethod,
            o.status,
            o.finalTotal
        )
        from OrderEntity o
        where o.shop.id = :shopId
          and (:code is null or lower(o.code) like lower(concat('%', :code, '%')))
          and (:status is null or :status = 'ALL' or o.status = :status)
          and (:shippingStatus is null or :shippingStatus = 'ALL' or o.shippingStatus = :shippingStatus)
          and (:paymentMethod is null or :paymentMethod = 'ALL' or o.paymentMethod = :paymentMethod)
          and (:fromDate is null or o.createdAt >= :fromDate)
          and (:toDate is null or o.createdAt <= :toDate)
        order by o.createdAt desc
    """)
    Page<ShopOrderResponse> findShopOrders(
            @Param("shopId") Integer shopId,
            @Param("code") String code,
            @Param("status") String status,
            @Param("shippingStatus") String shippingStatus,
            @Param("paymentMethod") String paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    List<OrderEntity> findTop5ByOrderByCreatedAtDesc();

    Optional<OrderEntity> findByGhnOrderCode(String code);
}
