package com.ttcs.menshop.modules.notification.entity;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private String type;
    private String title;
    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;

    private LocalDateTime createdAt;
}
