package com.ttcs.menshop.vnpay;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VnPayResponse {
    private boolean success;
    private boolean verified;
    private String message;
    private String orderCode;
    private Integer orderId;
    private BigDecimal finalTotal;
    private String responseCode;
    private String transactionStatus;
    private String transactionNo;
    private String bankCode;
    private String payDate;
}
