package com.ttcs.menshop.modules.cart_item.entity;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer quantity;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity productVariant;
}
