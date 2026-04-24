package com.ttcs.menshop.modules.product_variant.dto.request;

import lombok.Data;

@Data
public class ProductVariantRequest {
    private Integer id;
    private String color;
    private String size;
    private Integer stock;
}
