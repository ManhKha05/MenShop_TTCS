package com.ttcs.menshop.modules.dashboard.repository;

import com.ttcs.menshop.modules.dashboard.dto.MonthlyStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.OrderStatusStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.TopShopResponse;

import java.math.BigDecimal;
import java.util.List;

public interface DashboardRepositoryCustom {
    BigDecimal getTotalRevenue();
    Integer getTotalOrders();
    Integer getOrdersToday();
    Integer getTotalUsers();
    Integer getNewUsersThisMonth();
    Integer getActiveShops();
    Integer getPendingShops();
    Integer getPendingProducts();
    Integer getOutOfStockProducts();
    Integer getCancelledOrdersThisMonth();

    List<MonthlyStatResponse> getMonthlyStats(int months);
    List<OrderStatusStatResponse> getOrderStatusStats();
    List<TopShopResponse> getTopShops(int limit);
}
