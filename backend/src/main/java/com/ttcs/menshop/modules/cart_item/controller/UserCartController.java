package com.ttcs.menshop.modules.cart_item.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.cart_item.dto.request.UpdateQtyCartItemRequest;
import com.ttcs.menshop.modules.cart_item.dto.response.CartShopResponse;
import com.ttcs.menshop.modules.cart_item.service.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart-items")
public class UserCartController {

    private final CartItemService cartItemService;
    private final AuthService authService;

    public UserCartController(CartItemService cartItemService, AuthService authService) {
        this.cartItemService = cartItemService;
        this.authService = authService;
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartVariantIds(){
        int userId = authService.getCurrentUser().getId();
        List<Integer> variantIds = cartItemService.getCartVariantIds(userId);
        return ResponseEntity.ok(variantIds);
    }

    @GetMapping
    public ResponseEntity<?> getCartItems(){
        int userId = authService.getCurrentUser().getId();
        List<CartShopResponse> cartItems = cartItemService.findCartByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping
    public ResponseEntity<?> addToCart(
            @RequestParam Integer variantId,
            @RequestParam Integer quantity
    ) {
        int userId = authService.getCurrentUser().getId();
        cartItemService.addToCart(userId, variantId, quantity);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCart(@PathVariable Integer id, @RequestBody UpdateQtyCartItemRequest request){
        cartItemService.updateCartItem(id, request.getQuantity());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Integer id){
        cartItemService.removeFromCart(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
