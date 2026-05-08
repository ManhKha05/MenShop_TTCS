package com.ttcs.menshop.modules.product.service;

import com.ttcs.menshop.config.AIConfig;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.product.converter.ProductConverter;
import com.ttcs.menshop.modules.product.dto.request.AiRecommendRequest;
import com.ttcs.menshop.modules.product.dto.response.AiRecommendResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.product.repository.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    private final UserInteractionRepository userInteractionRepository;
    private final ProductConverter productConverter;
    private final AIConfig aiConfig;

    public List<ProductResponse> getHomeRecommendations(Integer userId) {
        String url = aiConfig.getRecommendEndpoint() + "/home";

        try {
            List<Integer> viewedIds = userInteractionRepository.findTop5RecentDistinctProductIds(userId);

            AiRecommendRequest requestBody = AiRecommendRequest.builder()
                    .user_id(userId)
                    .viewed_product_ids(viewedIds)
                    .build();

            AiRecommendResponse response = restTemplate.postForObject(url, requestBody, AiRecommendResponse.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                return getBestSeller();
            }

            List<Integer> orderedIds = response.getData();
            List<ProductEntity> products = productRepository.findByIdIn(orderedIds);

            return mapAndPreserveOrder(products, orderedIds);

        } catch (Exception e) {
            log.error("AI Server is down! Calling /home failed. Error: {}", e.getMessage());
            return getBestSeller();
        }
    }

    public List<ProductResponse> getDetailRecommendations(Integer userId, Integer targetProductId) {
        String url = aiConfig.getRecommendEndpoint() + "/detail";

        try {
            AiRecommendRequest requestBody = AiRecommendRequest.builder()
                    .user_id(userId)
                    .target_product_id(targetProductId)
                    .build();

            AiRecommendResponse response = restTemplate.postForObject(url, requestBody, AiRecommendResponse.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                return getFallbackDetailProducts(targetProductId);
            }

            List<Integer> recommendedIds = response.getData().stream()
                    .filter(id -> !id.equals(targetProductId))
                    .collect(Collectors.toList());

            if (recommendedIds.isEmpty()) {
                return getFallbackDetailProducts(targetProductId);
            }

            List<ProductEntity> products = productRepository.findByIdIn(recommendedIds);

            return mapAndPreserveOrder(products, recommendedIds);

        } catch (Exception e) {
            log.error("AI Server is down! Calling /detail failed. Error: {}", e.getMessage());
            return getFallbackDetailProducts(targetProductId);
        }
    }

    public List<ProductResponse> getBestSeller(){
        Pageable top20 = PageRequest.of(0, 20);
        List<ProductEntity> bestSellers = productRepository.getBestSeller(top20);

        return bestSellers.stream()
                .map(productConverter::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getFallbackDetailProducts(Integer currentProductId) {
        try {
            ProductEntity currentProduct = productRepository.findById(currentProductId)
                    .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại"));

            Integer categoryId = currentProduct.getCategory().getId();
            Pageable top8 = PageRequest.of(0, 8);

            List<ProductEntity> fallbackProducts = productRepository.findSimilarProductsByCategory(categoryId, currentProductId, top8);

            return fallbackProducts.stream()
                    .map(productConverter::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy dữ liệu Fallback: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ProductResponse> mapAndPreserveOrder(List<ProductEntity> dbProducts, List<Integer> orderedIds) {
        var productMap = dbProducts.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return orderedIds.stream()
                .map(productMap::get)
                .filter(java.util.Objects::nonNull)
                .map(productConverter::mapToResponse)
                .collect(Collectors.toList());
    }
}