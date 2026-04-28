package com.ttcs.menshop.modules.product.controller;

import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.product.dto.response.ProductDetailResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import com.ttcs.menshop.modules.product.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class UserProductController {

    private final ProductService productService;
    private final RecommendationService recommendationService;
    private final AuthService authService;

    public UserProductController(ProductService productService, RecommendationService recommendationService, AuthService authService) {
        this.productService = productService;
        this.recommendationService = recommendationService;
        this.authService = authService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        ProductDetailResponse res = productService.getProductDetail(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/recommendations/home")
    public ResponseEntity<List<ProductResponse>> getHomeRecs() {
        Integer userId = authService.getCurrentUser().getId();
        if (userId != null ) {
            return ResponseEntity.ok(recommendationService.getHomeRecommendations(userId));
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/recommendations/detail")
    public ResponseEntity<List<ProductResponse>> getDetailRecs(@RequestParam Integer productId) {
        Integer userId = authService.getCurrentUser().getId();
        if (userId != null) {
            return ResponseEntity.ok(recommendationService.getDetailRecommendations(userId, productId));
        } else {
            return ResponseEntity.ok(List.of());
        }
    }
}
