package com.ttcs.menshop.modules.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopOrderResponse {
    private Integer id;
    private String orderCode;
    private String receiverName;
    private String receiverPhone;
    private LocalDateTime createdAt;
    private String status;
    private String shippingStatus;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal finalPrice;
    private String shopName;
}
