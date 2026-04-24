package com.ttcs.menshop.modules.order_status.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderStatusHistoryResponse {
    private String status;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}
