package com.ttcs.menshop.modules.product.dto.response;

import com.ttcs.menshop.modules.product_variant.dto.response.ProductVariantResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductDetailResponse {
    private Integer id;
    private String name;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal flashPrice;
    private String description;
    private String status;
    private Integer shopId;
    private String shopName;
    private String shopImg;
    private Integer soldCount;
    private Integer viewCount;
    private BigDecimal ratingAvg;

    private Integer weight;
    private Integer width;
    private Integer height;
    private Integer length;

    private Integer categoryId;
    private String brandName;

    private Map<String, Object> attributesJson;

    private List<String> images;

    private List<ProductVariantResponse> variants;
}
