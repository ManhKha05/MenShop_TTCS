package com.ttcs.menshop.modules.order.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderHistoryResponse {
    private Integer id;
    private String orderCode;
    private String status;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private Integer shopId;
    private String shopName;
    private List<OrderHistoryItemResponse> items;
}
