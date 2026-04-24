package com.ttcs.menshop.modules.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStatsResponse {
    private Long totalProducts;
    private Long pending;
    private Long active;
    private Long inactive;
    private Long outOfStock;
}
