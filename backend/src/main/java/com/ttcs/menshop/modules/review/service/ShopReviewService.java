package com.ttcs.menshop.modules.review.service;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.review.dto.response.ShopReviewResponse;
import com.ttcs.menshop.modules.review.entity.ReviewEntity;
import com.ttcs.menshop.modules.review.repository.ReviewRepository;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopReviewService {

    private final ReviewRepository reviewRepository;
    private final AuthService authService;
    private final ShopRepository shopRepository;

    public Page<ShopReviewResponse> getReviews(Integer rating, String keyword, String sort, Pageable pageable) {

        UserEntity user = authService.getCurrentUser();
        ShopEntity shop = shopRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        Page<ReviewEntity> page = reviewRepository.findByShop(
                shop.getId(),
                rating,
                keyword,
                sort,
                pageable
        );

        return page.map(this::toResponse);
    }

    private ShopReviewResponse toResponse(ReviewEntity r) {
        return new ShopReviewResponse(
                r.getId(),
                r.getUser().getFullName(),
                r.getUser().getAvatar(),
                r.getProduct().getName(),
                r.getProduct().getImages().isEmpty()
                        ? null
                        : r.getProduct().getImages().get(0).getImageUrl(),
                r.getRating(),
                r.getContent(),
                r.getImages().stream().map(i -> i.getImageUrl()).toList(),
                r.getCreatedAt().toString()
        );
    }
}
