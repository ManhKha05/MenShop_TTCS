package com.ttcs.menshop.modules.dashboard.service;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.dashboard.dto.OrderStatusStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.ShopDashboardResponse;
import com.ttcs.menshop.modules.dashboard.repository.ShopDashboardRepository;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopDashboardService {

    private final AuthService authService;
    private final ShopRepository shopRepository;
    private final ShopDashboardRepository shopDashboardRepository;

    public ShopDashboardResponse getDashboard() {
        UserEntity currentUser = authService.getCurrentUser();

        ShopEntity shop = shopRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop của người dùng"));

        Integer shopId = shop.getId();

        ShopDashboardResponse response = new ShopDashboardResponse();

        ShopDashboardResponse.Overview overview = new ShopDashboardResponse.Overview();
        overview.setRevenueThisMonth(shopDashboardRepository.getRevenueThisMonth(shopId));
        overview.setRevenueGrowthPercent(calculateRevenueGrowthPercent(shopId));
        overview.setOrdersThisMonth(shopDashboardRepository.getOrdersThisMonth(shopId));
        overview.setPendingOrders(shopDashboardRepository.getPendingOrders(shopId));
        overview.setActiveProducts(shopDashboardRepository.getActiveProducts(shopId));
        overview.setOutOfStockProducts(shopDashboardRepository.getOutOfStockProducts(shopId));
        overview.setAverageRating(shopDashboardRepository.getAverageRating(shopId));
        overview.setTotalReviews(shopDashboardRepository.getTotalReviews(shopId));
        response.setOverview(overview);

        ShopDashboardResponse.Alerts alerts = new ShopDashboardResponse.Alerts();
        alerts.setPendingOrders(shopDashboardRepository.getPendingOrders(shopId));
        alerts.setLowStockProducts(shopDashboardRepository.getLowStockProducts(shopId));
        alerts.setOutOfStockProducts(shopDashboardRepository.getOutOfStockProducts(shopId));
        alerts.setLowRatingReviews(shopDashboardRepository.getLowRatingReviews(shopId));
        alerts.setPendingProducts(shopDashboardRepository.getPendingProducts(shopId));
        response.setAlerts(alerts);

        List<OrderStatusStatResponse> orderStatusStats = shopDashboardRepository.getOrderStatusStats(shopId);
        response.setOrderStatusStats(orderStatusStats);
        response.setMonthlyStats(shopDashboardRepository.getMonthlyStats(shopId, 6));
        response.setTopProducts(shopDashboardRepository.getTopProducts(shopId, 5));
        response.setRecentOrders(shopDashboardRepository.getRecentOrders(shopId, 8));

        int totalOrders = orderStatusStats.stream().mapToInt(i -> i.getCount() == null ? 0 : i.getCount()).sum();

        ShopDashboardResponse.Performance performance = new ShopDashboardResponse.Performance();
        performance.setDeliveredRate(calcRate(orderStatusStats, "DELIVERED", totalOrders));
        performance.setCancelledRate(calcRate(orderStatusStats, "CANCELLED", totalOrders));
        performance.setConfirmedRate(calcRate(orderStatusStats, "CONFIRMED", totalOrders));
        performance.setShippingSuccessRate(calcRate(orderStatusStats, "SHIPPING", totalOrders));
        response.setPerformance(performance);

        return response;
    }

    private Integer calcRate(List<OrderStatusStatResponse> stats, String status, int total) {
        if (total == 0) return 0;

        int value = stats.stream()
                .filter(i -> status.equals(i.getStatus()))
                .map(i -> i.getCount() == null ? 0 : i.getCount())
                .findFirst()
                .orElse(0);

        return value * 100 / total;
    }

    private Integer calculateRevenueGrowthPercent(Integer shopId) {
        var stats = shopDashboardRepository.getMonthlyStats(shopId, 2);
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
