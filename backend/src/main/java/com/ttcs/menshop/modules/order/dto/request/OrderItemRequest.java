package com.ttcs.menshop.modules.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private Integer cartItemId; // optional (null nếu mua ngay)
    private Integer variantId;
    private Integer quantity;
}
