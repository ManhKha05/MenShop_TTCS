package com.ttcs.menshop.modules.product.controller;

import com.ttcs.menshop.modules.product.dto.response.ProductDetailResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class UserProductController {

    private final ProductService productService;

    public UserProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        ProductDetailResponse res = productService.getProductDetail(id);
        return ResponseEntity.ok(res);
    }
}
