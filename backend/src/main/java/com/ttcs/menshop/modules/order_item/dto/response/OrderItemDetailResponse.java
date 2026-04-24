package com.ttcs.menshop.modules.order_item.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemDetailResponse {
    private Integer id;
    private String productName;
    private String image;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
