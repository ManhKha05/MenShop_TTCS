package com.ttcs.menshop.modules.product_image.entity;

import com.ttcs.menshop.modules.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_image")
@Data
public class ProductImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name = "image_url")
    private String imageUrl;
}
