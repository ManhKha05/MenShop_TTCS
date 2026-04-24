package com.ttcs.menshop.modules.payment_group.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDetailResponse {
    private String method;
    private String status;
    private LocalDateTime paidAt;
}
