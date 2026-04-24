package com.ttcs.menshop.modules.order.service;

import com.ttcs.menshop.modules.order.dto.request.OrderCreateRequest;
import com.ttcs.menshop.modules.order.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderCreateResponse createOrder(OrderCreateRequest request, HttpServletRequest httpRequest);
    OrderDetailResponse getOrderDetail(Integer id);
    void cancelOrder(Integer id);
    List<OrderHistoryResponse> getMyOrders(String status, String keyword);
    OrderStatsResponse getOrderStats(Integer shopId);
    Page<ShopOrderResponse> getShopOrders(
            int page,
            int size,
            String code,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );
    void updateOrderStatus(Integer orderId, String status);
}
