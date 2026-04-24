package com.ttcs.menshop.modules.product.service;

import com.ttcs.menshop.modules.product.dto.request.ProductRequest;
import com.ttcs.menshop.modules.product.dto.response.ProductCardResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductDetailResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.dto.response.ProductStatsResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    ProductStatsResponse getProductStats(Integer shopId);
    Page<ProductResponse> getProducts(Integer page, Integer size, String keyword, Integer categoryId, String status, Integer shopId);
    ProductDetailResponse getProductDetail(Integer productId);
    void create(ProductRequest productRequest);
    void update(Integer id, ProductRequest productRequest);
    void approve(Integer productId);
    void reject(Integer productId);
    void active(Integer productId);
    void inactive(Integer productId);
    List<ProductCardResponse> getBestSellingProductsByShop(Integer shopId);
    Page<ProductCardResponse> getProductsByShop(Integer shopId, Integer page, Integer size, Integer category, String sort, String priceOrder);

    Page<ProductCardResponse> getProductsByCategory(Integer categoryId, int page, int size, String sort, BigDecimal minPrice, BigDecimal maxPrice, Integer rating, List<Integer> childCategoryIds);
}
