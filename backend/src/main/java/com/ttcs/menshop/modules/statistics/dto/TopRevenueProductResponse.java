package com.ttcs.menshop.modules.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopRevenueProductResponse {
    private Integer productId;
    private String productName;
    private String image;
    private Integer soldCount;
    private BigDecimal revenue;
}
