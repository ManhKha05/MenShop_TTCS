package com.ttcs.menshop.modules.sale_product.entity;

import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.flash_sale.entity.SaleEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_product")
@Getter
@Setter
public class SaleProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal salePrice;

    @ManyToOne
    @JoinColumn(name = "flash_sale_id")
    private SaleEntity flashSale;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;


}
