package com.ttcs.menshop.modules.cart_item.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Integer cartItemId;
    private int quantity;

    private Integer variantId;
    private Integer stock;
    private String color;
    private String size;

    private Integer productId;
    private String productName;
//    private double price;
//    private double salePrice;
//    private double flashPrice;
    private BigDecimal displayPrice;
    private String imageUrl;

    private Integer shopId;
    private String shopName;
}
