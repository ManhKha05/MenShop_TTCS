//package com.ttcs.menshop.modules.product.entity;
//
//import com.ttcs.menshop.auth.entity.UserEntity;
//import com.ttcs.menshop.modules.product.enums.InteractionType;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "user_interaction", indexes = {
//        @Index(name = "idx_user_id", columnList = "user_id"),
//        @Index(name = "idx_product_id", columnList = "product_id"),
//        @Index(name = "idx_created_at", columnList = "created_at")
//})
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserInteractionEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private UserEntity user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
//    private ProductEntity product;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "interaction_type", nullable = false)
//    private InteractionType interactionType;
//
//    @Column(name = "weight_score", precision = 5, scale = 2)
//    @Builder.Default
//    private BigDecimal weightScore = BigDecimal.valueOf(1.00);
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//}