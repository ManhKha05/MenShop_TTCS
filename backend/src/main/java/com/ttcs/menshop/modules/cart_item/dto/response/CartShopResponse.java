package com.ttcs.menshop.modules.cart_item.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartShopResponse {
    private Integer shopId;
    private String shopName;
    private String shopLogo;
    private List<CartItemResponse> cartItems;
}
