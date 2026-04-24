package com.ttcs.menshop.modules.flash_sale.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaleRegisterRequest {
    private Integer flashSaleId;
    private List<Item> items;

    @Data
    public static class Item {
        private Integer productId;
        private BigDecimal flashPrice;
        private Boolean selected;
    }
}
