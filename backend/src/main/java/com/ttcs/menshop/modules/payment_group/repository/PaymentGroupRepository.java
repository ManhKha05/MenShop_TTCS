package com.ttcs.menshop.modules.payment_group.repository;

import com.ttcs.menshop.modules.payment_group.entity.PaymentGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentGroupRepository extends JpaRepository<PaymentGroupEntity, Integer> {
    Optional<PaymentGroupEntity> findByOrdersId(Integer orderId);
    Optional<PaymentGroupEntity> findByCode(String code);
}
