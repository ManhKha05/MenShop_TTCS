package com.ttcs.menshop.modules.review.controller;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.review.dto.request.ReviewCreateRequest;
import com.ttcs.menshop.modules.review.dto.response.ReviewResponse;
import com.ttcs.menshop.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final AuthService authService;

    public ReviewController(ReviewService reviewService, AuthService authService, AuthService authService1) {
        this.reviewService = reviewService;
        this.authService = authService1;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        UserEntity user = authService.getCurrentUser();
        return ResponseEntity.ok(reviewService.createReview(request, user.getEmail()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByProduct(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, PageRequest.of(page, size)));
    }

    @GetMapping("/can-review")
    public ResponseEntity<Boolean> canReview(
            @RequestParam Integer orderId,
            @RequestParam Integer productId
    ) {
        String email = authService.getCurrentUser().getEmail();
        return ResponseEntity.ok(reviewService.canReview(orderId, productId, email));
    }
}
