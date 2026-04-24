package com.ttcs.menshop.modules.flash_sale.entity;

import com.ttcs.menshop.modules.sale_product.entity.SaleProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flash_sale")
@Getter
@Setter
public class SaleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public SaleEntity(Integer id) {
        this.id = id;
    }

    private String name;
    private String description;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "is_disabled")
    private boolean isDisabled;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "flashSale")
    private List<SaleProductEntity> saleProducts;

    public SaleEntity() {

    }
}
