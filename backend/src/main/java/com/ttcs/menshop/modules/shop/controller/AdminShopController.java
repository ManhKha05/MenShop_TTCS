package com.ttcs.menshop.modules.shop.controller;

import com.ttcs.menshop.modules.shop.dto.request.UpdateStatusShopRequest;
import com.ttcs.menshop.modules.shop.dto.response.ShopResponse;
import com.ttcs.menshop.modules.shop.dto.response.ShopStatsResponse;
import com.ttcs.menshop.modules.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/shops")
public class AdminShopController {

    private final ShopService shopService;

    public AdminShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        ShopStatsResponse response = shopService.getShopStats();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort
    ) {
        Page<ShopResponse> responses = shopService.getAllShop(keyword, status, sort, page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllNoPagi() {
        List<ShopResponse> responses = shopService.getAllShopsNoPagi();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        ShopResponse response = shopService.getShopById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody UpdateStatusShopRequest request) {
        shopService.updateStatusShop(id, request);
        return ResponseEntity.ok(Map.of("message", "Update Status Successfully!"));
    }

}
