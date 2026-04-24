package com.ttcs.menshop.modules.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenueChartItemResponse {
    private String label;
    private Integer totalOrders;
    private Integer deliveredOrders;
    private Integer cancelledOrders;
    private BigDecimal revenue;
}
