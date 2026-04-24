package com.ttcs.menshop.notification.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ShopNotificationResponse {
    private String type;
    private Integer shopId;
    private Integer orderId;
    private String orderCode;
    private String message;
    private LocalDateTime createdAt;
}