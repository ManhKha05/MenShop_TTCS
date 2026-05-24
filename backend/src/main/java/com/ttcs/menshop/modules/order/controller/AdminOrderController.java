package com.ttcs.menshop.modules.order.controller;

import com.ttcs.menshop.modules.order.repository.OrderRepository;
import com.ttcs.menshop.modules.order.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public AdminOrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getOrdersStats()
    {
        return  ResponseEntity.ok(orderService.getOrderStats(null));
    }

    @GetMapping
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
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderRepository.findOrders(null, code, status, shippingStatus, paymentMethod, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer id) {
        return  ResponseEntity.ok(orderService.getOrderDetail(id));
    }
}
