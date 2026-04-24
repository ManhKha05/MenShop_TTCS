package com.ttcs.menshop.modules.product_variant.entity;

import com.ttcs.menshop.modules.cart_item.entity.CartItemEntity;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
public class ProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String sku;
    private String color;
    private String size;
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @OneToMany(mappedBy = "productVariant")
    private List<CartItemEntity> cartItems;
}
