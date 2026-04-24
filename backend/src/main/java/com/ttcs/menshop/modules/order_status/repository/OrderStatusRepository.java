package com.ttcs.menshop.modules.order_status.repository;

import com.ttcs.menshop.modules.order_status.entity.OrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusRepository extends JpaRepository<OrderStatusEntity,Integer> {
    List<OrderStatusEntity> findAllByOrderId(Integer orderId);
}
