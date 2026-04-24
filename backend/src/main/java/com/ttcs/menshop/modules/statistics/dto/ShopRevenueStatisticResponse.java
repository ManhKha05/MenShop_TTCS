package com.ttcs.menshop.modules.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShopRevenueStatisticResponse {
    private Overview overview;
    private List<RevenueChartItemResponse> chartData;
    private List<RevenueDetailItemResponse> detailRows;
    private List<TopRevenueProductResponse> topProducts;

    @Data
    public static class Overview {
        private BigDecimal totalRevenue;
        private Integer totalOrders;
        private Integer deliveredOrders;
        private Integer cancelledOrders;
        private BigDecimal averageOrderValue;
    }
}
