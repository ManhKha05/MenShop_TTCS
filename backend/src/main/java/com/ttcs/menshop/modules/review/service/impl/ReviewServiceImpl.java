package com.ttcs.menshop.modules.review.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.exception.EntityNotFoundException;
import com.ttcs.menshop.modules.notification.service.NotificationService;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order.repository.OrderRepository;
import com.ttcs.menshop.modules.order_item.repository.OrderItemRepository;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.review.dto.request.ReviewCreateRequest;
import com.ttcs.menshop.modules.review.dto.response.ReviewResponse;
import com.ttcs.menshop.modules.review.entity.ReviewEntity;
import com.ttcs.menshop.modules.review.repository.ReviewRepository;
import com.ttcs.menshop.modules.review.service.ReviewService;
import com.ttcs.menshop.modules.review_image.dto.response.ReviewImageResponse;
import com.ttcs.menshop.modules.review_image.entity.ReviewImageEntity;
import com.ttcs.menshop.websocket.dto.ProductSocketEvent;
import com.ttcs.menshop.websocket.service.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final WebSocketService webSocketService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng"));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này");
        }

        if (!"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ được đánh giá khi đơn hàng đã hoàn thành");
        }

        boolean productExistsInOrder = orderItemRepository.existsProductInOrder(order.getId(), product.getId());
        if (!productExistsInOrder) {
            throw new RuntimeException("Sản phẩm không tồn tại trong đơn hàng này");
        }

        boolean reviewed = reviewRepository.existsByUserIdAndProductIdAndOrderId(
                user.getId(), product.getId(), order.getId()
        );
        if (reviewed) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        ReviewEntity review = new ReviewEntity();
        review.setUser(user);
        review.setOrder(order);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setContent(request.getContent());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (String imageUrl : request.getImages()) {
                if (imageUrl == null || imageUrl.isBlank()) continue;

                ReviewImageEntity reviewImage = new ReviewImageEntity();
                reviewImage.setImageUrl(imageUrl);
                reviewImage.setReview(review);

                review.getImages().add(reviewImage);
            }
        }

        reviewRepository.save(review);

        updateProductRating(product);

        UserEntity shopOwner = product.getShop().getUser();

        String type = review.getRating() <= 2 ? "BAD_REVIEW" : "NEW_REVIEW";

        String title = review.getRating() <= 2
                ? "Có đánh giá thấp"
                : "Có đánh giá mới";

        String message = review.getRating() <= 2
                ? "Sản phẩm \"" + product.getName() + "\" vừa nhận đánh giá "
                + review.getRating() + " sao."
                : "Sản phẩm \"" + product.getName() + "\" vừa nhận đánh giá "
                + review.getRating() + " sao.";

        notificationService.createAndSend(
                shopOwner,
                null,
                type,
                title,
                message
        );

        webSocketService.sendToTopic("/topic/products", new ProductSocketEvent("PRODUCT_REVIEW", product.getId()));

        return new ReviewResponse(
                review.getId(),
                user.getId(),
                user.getFullName(),
                user.getAvatar(),
                product.getId(),
                order.getId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getImages().stream()
                        .map(img -> new ReviewImageResponse(img.getId(), img.getImageUrl()))
                        .toList()
        );
    }

    @Override
    public Page<ReviewResponse> getReviewsByProduct(Integer productId, Pageable pageable) {
        Page<ReviewEntity> entities = reviewRepository.findByProductId(productId, pageable);
        return entities.map(r -> new ReviewResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getFullName(),
                r.getUser().getAvatar(),
                r.getProduct().getId(),
                r.getOrder().getId(),
                r.getRating(),
                r.getContent(),
                r.getCreatedAt(),
                r.getImages().stream()
                        .map(img -> new ReviewImageResponse(img.getId(), img.getImageUrl()))
                        .toList()
        ));
    }

    @Override
    public boolean canReview(Integer orderId, Integer productId, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user"));

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) return false;
        if (!"DELIVERED".equals(order.getStatus())) return false;

        boolean productExistsInOrder = orderItemRepository.existsProductInOrder(orderId, productId);
        if (!productExistsInOrder) return false;

        return !reviewRepository.existsByUserIdAndProductIdAndOrderId(user.getId(), productId, orderId);
    }

    private void updateProductRating(ProductEntity product) {
        BigDecimal avg = reviewRepository.avgRatingByProductId(product.getId());
//        Long count = reviewRepository.countByProductId(product.getId());

        if (avg == null) avg = BigDecimal.ZERO;

         product.setRatingAvg(avg);
         productRepository.save(product);
    }
}
