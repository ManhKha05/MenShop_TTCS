package com.ttcs.menshop.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusStatResponse {
    private String status;
    private Integer count;
}
