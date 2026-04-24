package com.ttcs.menshop.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RecentShopOrderResponse {
    private Integer id;
    private String orderCode;
    private String customerName;
    private BigDecimal finalTotal;
    private String status;
    private String createdAt;
}
