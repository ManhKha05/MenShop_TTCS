package com.ttcs.menshop.modules.order.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderCreateResponse {
    private List<Integer> orderIds;
    private List<String> orderCodes;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentUrl;
    private BigDecimal finalTotal;
    private String message;
}
