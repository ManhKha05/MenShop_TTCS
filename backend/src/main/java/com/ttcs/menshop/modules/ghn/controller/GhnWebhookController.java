package com.ttcs.menshop.modules.ghn.controller;

import com.ttcs.menshop.email.EmailService;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order.repository.OrderRepository;
import com.ttcs.menshop.modules.order_status.entity.OrderStatusEntity;
import com.ttcs.menshop.modules.order_status.repository.OrderStatusRepository;
import com.ttcs.menshop.modules.ghn.dto.request.GhnWebhookRequest;
import com.ttcs.menshop.websocket.dto.OrderStatusEvent;
import com.ttcs.menshop.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/shipping/ghn")
@RequiredArgsConstructor
public class GhnWebhookController {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final WebSocketService webSocketService;
    private final EmailService emailService;

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestBody GhnWebhookRequest request
    ) {

        OrderEntity order = orderRepository
                .findByGhnOrderCode(request.getOrderCode())
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy đơn hàng"));

        String status = request.getStatus().toUpperCase();
        if(status.equals(order.getShippingStatus())) {
            return ResponseEntity.ok().build();
        }

        order.setShippingStatus(status);

        // ===== DELIVERING =====
        if(status.equals("DELIVERING")) {

            order.setShippedAt(LocalDateTime.now());

        }

        // ===== DELIVERED =====
        if(status.equals("DELIVERED")) {

            order.setDeliveredAt(LocalDateTime.now());

            order.setStatus("COMPLETED");

            if(order.getPaymentMethod().equals("COD")) {
                order.setPaymentStatus("PAID");
            }

            saveHistory(
                    order,
                    "COMPLETED",
                    "Đơn hàng đã hoàn tất"
            );
        }

        // ===== RETURNED =====
        if(status.equals("RETURNED")) {

            order.setStatus("CANCELLED");

            saveHistory(
                    order,
                    "CANCELLED",
                    "Đơn hàng đã hoàn về shop"
            );
        }

        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);

        saveHistory(
                order,
                status,
                mapShippingStatus(status)
        );

        emailService.sendOrderStatusEmail(
                order.getUser().getEmail(),
                order.getUser().getFullName(),
                order.getCode(),
                status
        );

        webSocketService.sendToTopic(
                "/topic/orders/" + order.getId(),
                new OrderStatusEvent(
                        "ORDER_UPDATED_STATUS",
                        order.getId()
                )
        );

        webSocketService.sendToTopic(
                "/topic/orders",
                new OrderStatusEvent(
                        "ORDER_UPDATED_STATUS",
                        order.getId()
                )
        );

        return ResponseEntity.ok().build();
    }

    private void saveHistory(
            OrderEntity order,
            String status,
            String note
    ) {

        OrderStatusEntity history = new OrderStatusEntity();

        history.setOrder(order);
        history.setStatus(status);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());

        orderStatusRepository.save(history);
    }

    private String mapShippingStatus(String status) {

        return switch (status) {

            case "READY_TO_PICK" ->
                    "Đơn hàng đang chờ lấy hàng";

            case "PICKING" ->
                    "Shipper đang lấy hàng";

            case "PICKED" ->
                    "Đơn hàng đã được lấy";

            case "TRANSPORTING" ->
                    "Đơn hàng đang trung chuyển";

            case "SORTING" ->
                    "Đơn hàng đang phân loại";

            case "DELIVERING" ->
                    "Đơn hàng đang được giao";

            case "DELIVERED" ->
                    "Đơn hàng đã được giao thành công";

            case "DELIVERY_FAIL" ->
                    "Giao hàng thất bại";

            case "WAITING_TO_RETURN" ->
                    "Đơn hàng đang chờ hoàn";

            case "RETURNING" ->
                    "Đơn hàng đang hoàn về shop";

            case "RETURNED" ->
                    "Đơn hàng đã hoàn về shop";

            case "CANCEL" ->
                    "Đơn vận chuyển đã bị hủy";

            default ->
                    "Cập nhật trạng thái vận chuyển";
        };
    }
}