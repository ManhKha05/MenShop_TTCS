package com.ttcs.menshop.modules.dashboard.service;

import com.ttcs.menshop.modules.dashboard.dto.AdminDashboardResponse;
import com.ttcs.menshop.modules.dashboard.repository.DashboardRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final DashboardRepositoryCustom dashboardRepositoryCustom;
    private final DashboardActivityService dashboardActivityService;

    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        AdminDashboardResponse.Overview overview = new AdminDashboardResponse.Overview();
        overview.setTotalRevenue(dashboardRepositoryCustom.getTotalRevenue());
        overview.setTotalOrders(dashboardRepositoryCustom.getTotalOrders());
        overview.setOrdersToday(dashboardRepositoryCustom.getOrdersToday());
        overview.setTotalUsers(dashboardRepositoryCustom.getTotalUsers());
        overview.setNewUsersThisMonth(dashboardRepositoryCustom.getNewUsersThisMonth());
        overview.setActiveShops(dashboardRepositoryCustom.getActiveShops());
        overview.setPendingShops(dashboardRepositoryCustom.getPendingShops());
        overview.setRevenueGrowthPercent(calculateRevenueGrowthPercent());
        response.setOverview(overview);

        AdminDashboardResponse.Alerts alerts = new AdminDashboardResponse.Alerts();
        alerts.setPendingProducts(dashboardRepositoryCustom.getPendingProducts());
        alerts.setPendingShops(dashboardRepositoryCustom.getPendingShops());
        alerts.setOutOfStockProducts(dashboardRepositoryCustom.getOutOfStockProducts());
        alerts.setCancelledOrdersThisMonth(dashboardRepositoryCustom.getCancelledOrdersThisMonth());
        response.setAlerts(alerts);

        response.setMonthlyStats(dashboardRepositoryCustom.getMonthlyStats(6));
        response.setOrderStatusStats(dashboardRepositoryCustom.getOrderStatusStats());
        response.setTopShops(dashboardRepositoryCustom.getTopShops(5));
        response.setRecentActivities(dashboardActivityService.getRecentActivities());

        AdminDashboardResponse.Performance performance = new AdminDashboardResponse.Performance();
        Integer totalOrders = overview.getTotalOrders();

        if (totalOrders == 0) {
            performance.setDeliveredRate(0);
            performance.setCancelledRate(0);
            performance.setConfirmedRate(0);
        } else {
            int delivered = getStatusCount("DELIVERED");
            int cancelled = getStatusCount("CANCELLED");
            int confirmed = getStatusCount("CONFIRMED");

            performance.setDeliveredRate(delivered * 100 / totalOrders);
            performance.setCancelledRate(cancelled * 100 / totalOrders);
            performance.setConfirmedRate(confirmed * 100 / totalOrders);
        }

        response.setPerformance(performance);
        return response;
    }

    private int getStatusCount(String status) {
        return dashboardRepositoryCustom.getOrderStatusStats().stream()
                .filter(item -> status.equals(item.getStatus()))
                .map(item -> item.getCount() == null ? 0 : item.getCount())
                .findFirst()
                .orElse(0);
    }

    private Integer calculateRevenueGrowthPercent() {
        var stats = dashboardRepositoryCustom.getMonthlyStats(2);
        if (stats.size() < 2) return 0;

        BigDecimal previous = stats.get(0).getRevenue();
        BigDecimal current = stats.get(1).getRevenue();

        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
