package com.ttcs.menshop.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopShopResponse {
    private Integer shopId;
    private String shopName;
    private Integer totalOrders;
    private BigDecimal revenue;
}
