package com.ttcs.menshop.modules.category.service;

import com.ttcs.menshop.modules.category.dto.request.CategoryRequest;
import com.ttcs.menshop.modules.category.dto.response.CategoryDetailResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryStatsResponse;
import com.ttcs.menshop.modules.category.entity.CategoryEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getActiveParentCategories();
    CategoryStatsResponse getCategoryStats();
    Page<CategoryResponse> searchCategories(String keyword, Integer parentId, String status, Integer page, Integer size);
    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest);
    List<CategoryResponse> getLeafCategories();
    List<CategoryResponse> getByShopId(Integer shopId);

    CategoryDetailResponse getCategoryById(Integer id);
    List<Integer> getCategoryIdsForProductQuery(CategoryEntity category);
}
