package com.ttcs.menshop.modules.review.service;

import com.ttcs.menshop.modules.review.dto.request.ReviewCreateRequest;
import com.ttcs.menshop.modules.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse createReview(ReviewCreateRequest request, String email);
    Page<ReviewResponse> getReviewsByProduct(Integer productId, Pageable pageable);
    boolean canReview(Integer orderId, Integer productId, String email);
}
