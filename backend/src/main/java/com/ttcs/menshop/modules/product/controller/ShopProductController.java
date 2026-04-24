package com.ttcs.menshop.modules.product.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.product.dto.request.ProductRequest;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductStatsResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop/products")
public class ShopProductController {

    private final ProductService productService;
    private final AuthService authService;

    public ShopProductController(ProductService productService, AuthService authService, AuthService authService1) {
        this.productService = productService;
        this.authService = authService1;
    }

    @GetMapping("/stats")
    public ResponseEntity<ProductStatsResponse> getProductStats() {
        int shopId = authService.getCurrentUser().getShop().getId();
        ProductStatsResponse stats = productService.getProductStats(shopId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam (required = false) String status
    ) {
        int shopId = authService.getCurrentUser().getShop().getId();
        Page<ProductResponse>  responses= productService.getProducts(page, size, keyword, categoryId, status, shopId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest req) {
        productService.create(req);
        return ResponseEntity.ok().body("success");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody ProductRequest req
    ) {
        productService.update(id, req);
        return ResponseEntity.ok().body("success");
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<?> active(@PathVariable Integer id) {
        productService.active(id);
        return ResponseEntity.ok("Activated");
    }

    @PutMapping("/{id}/inactive")
    public ResponseEntity<?> inactive(@PathVariable Integer id) {
        productService.inactive(id);
        return ResponseEntity.ok("Inactivated");
    }
}
