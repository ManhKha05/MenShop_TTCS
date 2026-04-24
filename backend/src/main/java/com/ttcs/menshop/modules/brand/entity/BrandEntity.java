package com.ttcs.menshop.modules.brand.entity;

import com.ttcs.menshop.modules.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "brand")
@Data
public class BrandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "brand")
    private List<ProductEntity> products;
}
