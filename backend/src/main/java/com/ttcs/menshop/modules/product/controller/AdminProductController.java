package com.ttcs.menshop.modules.product.controller;

import com.ttcs.menshop.modules.product.dto.response.ProductDetailResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(productService.getProductStats(null));
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam (required = false) String status,
            @RequestParam (required = false) Integer shopId
    ) {
        Page<ProductResponse> responses= productService.getProducts(page, size, keyword, categoryId, status, shopId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(
            @PathVariable Integer id
    ) {
        ProductDetailResponse res = productService.getProductDetail(id);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Integer id) {
        productService.approve(id);
        return ResponseEntity.ok("Approved");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Integer id) {
        productService.reject(id);
        return ResponseEntity.ok("Rejected");
    }

    @PutMapping("/{id}/inactive")
    public ResponseEntity<?> inactive(@PathVariable Integer id) {
        productService.inactive(id);
        return ResponseEntity.ok("Inactivated");
    }

}
