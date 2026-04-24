package com.ttcs.menshop.modules.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShopDashboardResponse {
    private Overview overview;
    private Alerts alerts;
    private Performance performance;
    private List<MonthlyStatResponse> monthlyStats;
    private List<OrderStatusStatResponse> orderStatusStats;
    private List<TopProductResponse> topProducts;
    private List<RecentShopOrderResponse> recentOrders;

    @Data
    public static class Overview {
        private BigDecimal revenueThisMonth;
        private Integer revenueGrowthPercent;
        private Integer ordersThisMonth;
        private Integer pendingOrders;
        private Integer activeProducts;
        private Integer outOfStockProducts;
        private BigDecimal averageRating;
        private Integer totalReviews;
    }

    @Data
    public static class Alerts {
        private Integer pendingOrders;
        private Integer lowStockProducts;
        private Integer outOfStockProducts;
        private Integer lowRatingReviews;
        private Integer pendingProducts;
    }

    @Data
    public static class Performance {
        private Integer deliveredRate;
        private Integer cancelledRate;
        private Integer confirmedRate;
        private Integer shippingSuccessRate;
    }
}
