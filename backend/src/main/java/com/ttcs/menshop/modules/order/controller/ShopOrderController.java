package com.ttcs.menshop.modules.order.controller;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class ShopOrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public ShopOrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @GetMapping("/shop/orders/stats")
    public ResponseEntity<?> getOrderStats() {
        UserEntity user = authService.getCurrentUser();
        return  ResponseEntity.ok(orderService.getOrderStats(user.getShop().getId()));
    }

    @GetMapping("/shop/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String shippingStatus,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate
    ) {
        return ResponseEntity.ok(orderService.getShopOrders(page, size, code, status, shippingStatus, paymentMethod, fromDate, toDate));
    }

    @GetMapping("/shop/orders/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer id) {
        return  ResponseEntity.ok(orderService.getOrderDetail(id));
    }

    @PatchMapping("/shop/orders/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Integer id) {
        orderService.updateOrderStatus(id, "CONFIRMED");
        return ResponseEntity.ok(Map.of("message", "Xác nhận đơn hàng thành công"));
    }

    @PatchMapping("/shop/orders/{id}/delivering")
    public ResponseEntity<?> deliveringOrder(@PathVariable Integer id) {
        orderService.updateOrderStatus(id, "DELIVERING");
        return ResponseEntity.ok(Map.of("message", "Chuyển sang giao hàng thành công"));
    }

    @PatchMapping("/shop/orders/{id}/delivered")
    public ResponseEntity<?> deliveredOrder(@PathVariable Integer id) {
        orderService.updateOrderStatus(id, "DELIVERED");
        return ResponseEntity.ok(Map.of("message", "Cập nhật đã giao thành công"));
    }

    @PatchMapping("/shop/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id) {
        orderService.updateOrderStatus(id, "CANCELLED");
        return ResponseEntity.ok(Map.of("message", "Hủy đơn hàng thành công"));
    }

}
