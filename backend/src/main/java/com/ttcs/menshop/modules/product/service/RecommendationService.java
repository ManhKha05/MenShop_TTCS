//package com.ttcs.menshop.modules.product.service;
//
//import com.ttcs.menshop.config.AIConfig;
//import com.ttcs.menshop.modules.product.converter.ProductConverter;
//import com.ttcs.menshop.modules.product.dto.request.AiRecommendRequest;
//import com.ttcs.menshop.modules.product.dto.response.AiRecommendResponse;
//import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
//import com.ttcs.menshop.modules.product.entity.ProductEntity;
//import com.ttcs.menshop.modules.product.repository.ProductRepository;
//import com.ttcs.menshop.modules.product.repository.UserInteractionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RecommendationService {
//
//    private final ProductRepository productRepository;
//    private final RestTemplate restTemplate;
//    private final UserInteractionRepository userInteractionRepository;
//    private final ProductConverter productConverter;
//    private final AIConfig aiConfig;
//
//    public List<ProductResponse> getHomeRecommendations(Integer userId) {
//        String url = aiConfig.getRecommendEndpoint() + "/home";
//
//        try {
//            List<Integer> viewedIds = userInteractionRepository.findTop5RecentDistinctProductIds(userId);
//
//            AiRecommendRequest requestBody = AiRecommendRequest.builder()
//                    .user_id(userId)
//                    .viewed_product_ids(viewedIds)
//                    .build();
//
//            AiRecommendResponse response = restTemplate.postForObject(url, requestBody, AiRecommendResponse.class);
//
//            if (response == null || response.getData() == null || response.getData().isEmpty()) {
//                return List.of();
//            }
//
//            List<ProductEntity> products = productRepository.findByIdIn(response.getData());
//
//            return products.stream()
//                    .map(productConverter::mapToResponse)
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("AI Server is down! Calling /home failed. Error: {}", e.getMessage());
//            return List.of();
//        }
//    }
//
//    public List<ProductResponse> getDetailRecommendations(Integer userId, Integer targetProductId) {
//        String url = aiConfig.getRecommendEndpoint() + "/detail";
//
//        try {
//            AiRecommendRequest requestBody = AiRecommendRequest.builder()
//                    .user_id(userId)
//                    .target_product_id(targetProductId)
//                    .build();
//
//            AiRecommendResponse response = restTemplate.postForObject(url, requestBody, AiRecommendResponse.class);
//
//            if (response == null || response.getData() == null || response.getData().isEmpty()) {
//                return List.of();
//            }
//
//            List<Integer> recommendedIds = response.getData().stream()
//                    .filter(id -> !id.equals(targetProductId))
//                    .collect(Collectors.toList());
//
//            if (recommendedIds.isEmpty()) {
//                return List.of();
//            }
//
//            List<ProductEntity> products = productRepository.findByIdIn(recommendedIds);
//
//            return products.stream()
//                    .map(productConverter::mapToResponse)
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("AI Server is down! Calling /detail failed. Error: {}", e.getMessage());
//            return List.of();
//        }
//    }
//}