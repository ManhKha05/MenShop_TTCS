package com.ttcs.menshop.modules.review.controller;

import com.ttcs.menshop.modules.review.dto.response.ShopReviewResponse;
import com.ttcs.menshop.modules.review.service.ShopReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop/reviews")
@RequiredArgsConstructor
public class ShopReviewController {

    private final ShopReviewService shopReviewService;

    @GetMapping
    public Page<ShopReviewResponse> getReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return shopReviewService.getReviews(rating, keyword, sort, pageable);
    }
}
