package com.ttcs.menshop.modules.order.controller;

import com.ttcs.menshop.modules.order.dto.request.OrderCreateRequest;
import com.ttcs.menshop.modules.order.dto.response.OrderCreateResponse;
import com.ttcs.menshop.modules.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class CustomerOrderController {

    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(orderService.getMyOrders(status, keyword));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderCreateRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(orderService.createOrder(request, httpRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Integer id) {
        return  ResponseEntity.ok(orderService.getOrderDetail(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}
