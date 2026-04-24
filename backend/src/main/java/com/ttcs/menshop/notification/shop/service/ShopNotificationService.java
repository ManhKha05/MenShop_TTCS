package com.ttcs.menshop.notification.shop.service;

import com.ttcs.menshop.notification.shop.dto.ShopNotificationResponse;
import com.ttcs.menshop.websocket.service.WebSocketService;
import org.springframework.stereotype.Service;

@Service
public class ShopNotificationService {

    private final WebSocketService webSocketService;

    public ShopNotificationService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public void notifyNewOrder(Integer shopId, Integer orderId, String orderCode) {
        ShopNotificationResponse res = new ShopNotificationResponse();
        res.setType("NEW_ORDER");
        res.setShopId(shopId);
        res.setOrderId(orderId);
        res.setOrderCode(orderCode);
        res.setMessage("Bạn có đơn hàng mới: " + orderCode);
        res.setCreatedAt(java.time.LocalDateTime.now());

        webSocketService.sendToTopic(
                "/topic/shop/" + shopId + "/notifications",
                res
        );
    }
}
