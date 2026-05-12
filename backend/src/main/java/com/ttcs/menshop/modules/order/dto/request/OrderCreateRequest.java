package com.ttcs.menshop.modules.order.dto.request;

import com.ttcs.menshop.modules.shipping.dto.request.ShopShippingRequest;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private Integer addressId;
    private String paymentMethod;  // cod, vnpay
    private List<OrderItemRequest> items;
    private String note;
    private List<ShopShippingRequest> shippingFees;
}
