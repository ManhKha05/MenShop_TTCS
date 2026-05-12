package com.ttcs.menshop.modules.order.entity;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.notification.entity.NotificationEntity;
import com.ttcs.menshop.modules.order_item.entity.OrderItemEntity;
import com.ttcs.menshop.modules.order_status.entity.OrderStatusEntity;
import com.ttcs.menshop.modules.payment_group.entity.PaymentGroupEntity;
import com.ttcs.menshop.modules.review.entity.ReviewEntity;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Integer receiverDistrictId;
    private String receiverWardCode;
    private BigDecimal totalPrice;
    private BigDecimal shippingFee;
    private BigDecimal finalTotal;
    private Integer shippingServiceId;
    private String shippingServiceName;
    private String ghnOrderCode;
    private String shippingStatus;
    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private String note;

    @Column(name = "created_at",  insertable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private ShopEntity shop;

    @OneToMany(mappedBy = "order")
    private List<OrderItemEntity> orderItems;

    @OneToMany(mappedBy = "order")
    private List<OrderStatusEntity> orderStatuses;

    @ManyToOne
    @JoinColumn(name = "payment_group_id")
    private PaymentGroupEntity paymentGroup;

    @OneToMany(mappedBy = "order")
    private List<NotificationEntity> notifications;

    @OneToMany(mappedBy = "order")
    private List<ReviewEntity>  reviews;
}
