package com.ttcs.menshop.modules.product.entity;

import com.ttcs.menshop.modules.brand.entity.BrandEntity;
import com.ttcs.menshop.modules.category.entity.CategoryEntity;
import com.ttcs.menshop.modules.product_image.entity.ProductImageEntity;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.review.entity.ReviewEntity;
import com.ttcs.menshop.modules.sale_product.entity.SaleProductEntity;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "product")
@Data
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public ProductEntity(Integer id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private ShopEntity shop;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private BrandEntity brand;

    @Column(nullable = false)
    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    private String description;

//    @Type(JsonType.class)
@JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes_json", columnDefinition = "json")
    private Map<String, Object> attributesJson;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @Column(name = "rating_avg", precision = 3, scale = 2, columnDefinition = "decimal(3,2) default 0.0")
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    private String status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImageEntity> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleProductEntity> saleProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ReviewEntity> reviews = new ArrayList<>();

    public ProductEntity() {

    }
}
