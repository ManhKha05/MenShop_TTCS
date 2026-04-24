package com.ttcs.menshop.modules.shop.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.shop.dto.request.ShopRequest;
import com.ttcs.menshop.modules.shop.dto.response.ShopResponse;
import com.ttcs.menshop.modules.shop.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shops")
public class ShopShopController {

    private final ShopService shopService;
    private final AuthService authService;

    public ShopShopController(ShopService shopService, AuthService authService) {
        this.shopService = shopService;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        int userId = authService.getCurrentUser().getId();
        ShopResponse response = shopService.getShopProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ShopRequest shopRequest) {
        int userId = authService.getCurrentUser().getId();
        shopService.updateProfileShop(userId, shopRequest);
        return ResponseEntity.ok().body(Map.of("message", "Cập nhật thông tin cửa hàng thành công!"));
    }
}
