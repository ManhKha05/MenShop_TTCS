package com.ttcs.menshop.modules.product.controller;

import com.ttcs.menshop.modules.product.dto.response.ProductDetailResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import com.ttcs.menshop.modules.product.service.RecommendationService;
import com.ttcs.menshop.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class UserProductController {

    private final ProductService productService;
    private final RecommendationService recommendationService;

    public UserProductController(ProductService productService, RecommendationService recommendationService) {
        this.productService = productService;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        ProductDetailResponse res = productService.getProductDetail(id);
        return ResponseEntity.ok(res);
    }
//
//    @GetMapping("/recommendations/home")
//    public ResponseEntity<List<ProductResponse>> getHomeRecs(
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        if (userDetails != null) {
//            Integer userId = userDetails.getUser().getId();
//            return ResponseEntity.ok(recommendationService.getHomeRecommendations(userId));
//        } else {
//            return ResponseEntity.ok(List.of());
//        }
//    }
//
//    @GetMapping("/recommendations/detail")
//    public ResponseEntity<List<ProductResponse>> getDetailRecs(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestParam Integer productId) {
//
//        if (userDetails != null) {
//            Integer userId = userDetails.getUser().getId();
//            return ResponseEntity.ok(recommendationService.getDetailRecommendations(userId, productId));
//        } else {
//            return ResponseEntity.ok(List.of());
//        }
//    }
}
