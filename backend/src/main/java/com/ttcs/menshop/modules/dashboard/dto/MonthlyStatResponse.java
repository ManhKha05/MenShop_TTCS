package com.ttcs.menshop.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyStatResponse {
    private String monthLabel;
    private Integer orders;
    private BigDecimal revenue;
}
