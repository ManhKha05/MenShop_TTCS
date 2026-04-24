package com.ttcs.menshop.modules.product_variant.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductVariantResponse {
    private Integer id;
    private String sku;
    private String color;
    private String size;
    private Integer stock;
    private Map<String, Object> attributes;
}
