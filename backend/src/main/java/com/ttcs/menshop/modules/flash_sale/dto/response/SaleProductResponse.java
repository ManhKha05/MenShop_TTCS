package com.ttcs.menshop.modules.flash_sale.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleProductResponse {
    private Integer id;
    private String image;
    private String name;
    private BigDecimal price;
    private Integer stock;

    private Boolean isRegistered;
    private BigDecimal flashPrice;
}
