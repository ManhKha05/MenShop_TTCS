package com.ttcs.menshop.modules.cart_item.service;

import com.ttcs.menshop.modules.cart_item.dto.response.CartItemResponse;
import com.ttcs.menshop.modules.cart_item.dto.response.CartShopResponse;

import java.util.List;

public interface CartItemService {
    List<Integer> getCartVariantIds(Integer userId);
    List<CartShopResponse> findCartByUserId(Integer userId);
    void addToCart(Integer userId, Integer variantId, Integer quantity);
    void updateCartItem(Integer id, Integer quantity);
    void removeFromCart(Integer itemId);
}
