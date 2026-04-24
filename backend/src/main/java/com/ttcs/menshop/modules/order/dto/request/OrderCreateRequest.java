package com.ttcs.menshop.modules.order.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private Integer addressId;
    private String shippingMethod; // fast, economy
    private String paymentMethod;  // cod, vnpay
    private List<OrderItemRequest> items;
    private String note;
}
