package com.ttcs.menshop.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopProductResponse {
    private Integer productId;
    private String productName;
    private String image;
    private Integer soldCount;
    private Integer stock;
    private BigDecimal revenue;
}
