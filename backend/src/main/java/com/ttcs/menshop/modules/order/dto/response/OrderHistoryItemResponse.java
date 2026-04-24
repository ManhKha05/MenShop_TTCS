package com.ttcs.menshop.modules.order.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderHistoryItemResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private String image;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal price;
}
