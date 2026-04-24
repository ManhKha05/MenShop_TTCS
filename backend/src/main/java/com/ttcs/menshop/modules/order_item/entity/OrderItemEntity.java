package com.ttcs.menshop.modules.order_item.entity;

import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Getter
@Setter
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private ShopEntity shop;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private ProductVariantEntity variant;
}
