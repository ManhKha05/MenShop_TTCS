package com.ttcs.menshop.modules.payment_group.entity;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment_group")
@Getter
@Setter
public class PaymentGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String method;
    private BigDecimal totalAmount;
    private String status;
    private String transactionCode;
    private LocalDateTime paidAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "paymentGroup")
    private List<OrderEntity> orders;
}
