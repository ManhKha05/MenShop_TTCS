package com.ttcs.menshop.modules.dashboard.repository;

import com.ttcs.menshop.modules.dashboard.dto.MonthlyStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.OrderStatusStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.RecentShopOrderResponse;
import com.ttcs.menshop.modules.dashboard.dto.TopProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ShopDashboardRepository {
    BigDecimal getRevenueThisMonth(Integer shopId);
    Integer getOrdersThisMonth(Integer shopId);
    Integer getPendingOrders(Integer shopId);
    Integer getActiveProducts(Integer shopId);
    Integer getOutOfStockProducts(Integer shopId);
    Integer getLowStockProducts(Integer shopId);
    Integer getPendingProducts(Integer shopId);
    BigDecimal getAverageRating(Integer shopId);
    Integer getTotalReviews(Integer shopId);
    Integer getLowRatingReviews(Integer shopId);

    List<MonthlyStatResponse> getMonthlyStats(Integer shopId, int months);
    List<OrderStatusStatResponse> getOrderStatusStats(Integer shopId);
    List<TopProductResponse> getTopProducts(Integer shopId, int limit);
    List<RecentShopOrderResponse> getRecentOrders(Integer shopId, int limit);
}
