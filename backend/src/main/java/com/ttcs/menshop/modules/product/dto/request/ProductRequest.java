package com.ttcs.menshop.modules.product.dto.request;

import com.ttcs.menshop.modules.product_variant.dto.request.ProductVariantRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductRequest {
    private String name;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String description;
    private Integer categoryId;
    private String status;
    private String brandName;

    private List<String> images;
    private List<ProductVariantRequest> variants;
    private Map<String, Object> attributesJson;
}
