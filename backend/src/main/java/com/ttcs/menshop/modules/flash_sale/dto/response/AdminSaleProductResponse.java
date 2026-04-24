package com.ttcs.menshop.modules.flash_sale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AdminSaleProductResponse {
    private Integer id;
    private String name;
    private String image;
    private String shop;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
}
