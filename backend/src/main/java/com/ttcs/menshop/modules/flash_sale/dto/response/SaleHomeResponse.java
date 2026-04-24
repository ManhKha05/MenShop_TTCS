package com.ttcs.menshop.modules.flash_sale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SaleHomeResponse {
    private Integer id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Item> items;

    @Data
    @AllArgsConstructor
    public static class Item {
        private Integer productId;
        private String name;
        private String image;
        private BigDecimal originalPrice;
        private BigDecimal flashPrice;
        private Integer stock;
    }
}
