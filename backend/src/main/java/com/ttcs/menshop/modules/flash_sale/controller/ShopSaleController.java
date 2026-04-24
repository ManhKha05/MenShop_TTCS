package com.ttcs.menshop.modules.flash_sale.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.product.service.ProductService;
import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRegisterRequest;
import com.ttcs.menshop.modules.flash_sale.dto.response.SaleProductResponse;
import com.ttcs.menshop.modules.flash_sale.dto.response.SaleResponse;
import com.ttcs.menshop.modules.flash_sale.service.SaleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop/flash-sale")
public class ShopSaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final AuthService authService;

    public ShopSaleController(SaleService saleService, ProductService productService, AuthService authService) {
        this.saleService = saleService;
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        Page<SaleResponse> all = saleService.getAll(keyword, status, page, size);
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{flashSaleId}/products")
    public ResponseEntity<?> getProducts(
            @PathVariable Integer flashSaleId,
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword
    ) {
        int shopId = authService.getCurrentUser().getShop().getId();
        Page<SaleProductResponse>  responses= saleService.getProductsForFlashSale(flashSaleId, page, size, keyword, shopId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SaleRegisterRequest req) {
        saleService.registerProducts(req);
        return ResponseEntity.ok("OK");
    }
}
