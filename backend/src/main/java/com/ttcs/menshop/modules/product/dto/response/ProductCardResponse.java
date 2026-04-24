package com.ttcs.menshop.modules.product.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCardResponse {
    private Integer id;
    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal flashSalePrice;
    private String shopName;
    private BigDecimal rating;
    private Integer sold;
}
