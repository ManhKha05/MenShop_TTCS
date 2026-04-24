package com.ttcs.menshop.modules.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminDashboardResponse {
    private Overview overview;
    private Alerts alerts;
    private Performance performance;
    private List<MonthlyStatResponse> monthlyStats;
    private List<OrderStatusStatResponse> orderStatusStats;
    private List<TopShopResponse> topShops;
    private List<RecentActivityResponse> recentActivities;

    @Data
    public static class Overview {
        private BigDecimal totalRevenue;
        private Integer totalOrders;
        private Integer ordersToday;
        private Integer totalUsers;
        private Integer newUsersThisMonth;
        private Integer activeShops;
        private Integer pendingShops;
        private Integer revenueGrowthPercent;
    }

    @Data
    public static class Alerts {
        private Integer pendingProducts;
        private Integer pendingShops;
        private Integer outOfStockProducts;
        private Integer cancelledOrdersThisMonth;
    }

    @Data
    public static class Performance {
        private Integer deliveredRate;
        private Integer cancelledRate;
        private Integer confirmedRate;
    }
}
