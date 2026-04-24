package com.ttcs.menshop.modules.category.service.impl;

import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.category.converter.CategoryConverter;
import com.ttcs.menshop.modules.category.dto.request.CategoryRequest;
import com.ttcs.menshop.modules.category.dto.response.CategoryDetailResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryItemResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryStatsResponse;
import com.ttcs.menshop.modules.category.entity.CategoryEntity;
import com.ttcs.menshop.modules.category.repository.CategoryRepository;
import com.ttcs.menshop.modules.category.service.CategoryService;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryConverter categoryConverter;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryConverter categoryConverter, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryConverter = categoryConverter;
        this.productRepository = productRepository;
    }

    @Override
    public List<CategoryResponse> getActiveParentCategories() {
        return categoryRepository.findByParentIsNullAndStatus("ACTIVE")
                .stream().map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getImageUrl(),
                        c.getStatus(),
                        null,
                        null,
                        c.getCreatedAt()
                )).toList();
    }

    @Override
    public CategoryStatsResponse getCategoryStats() {
        Long total = categoryRepository.count();
        Long active = categoryRepository.countByStatus("ACTIVE");
        Long inactive = categoryRepository.countByStatus("INACTIVE");
        Long parent = categoryRepository.countByParentId(null);

        return new  CategoryStatsResponse(total, active, inactive, parent);
    }

    @Override
    public Page<CategoryResponse> searchCategories(String keyword, Integer parentId, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<CategoryEntity> categoryEntities = categoryRepository.search(keyword, parentId, status,  pageable);
        Page<CategoryResponse> categoryResponses = categoryEntities
                .map(categoryEntity -> {
                    CategoryResponse categoryResponse = new CategoryResponse();
                    categoryResponse.setId(categoryEntity.getId());
                    categoryResponse.setName(categoryEntity.getName());
                    categoryResponse.setImageUrl(categoryEntity.getImageUrl());
                    categoryResponse.setStatus(categoryEntity.getStatus());
                    categoryResponse.setCreatedAt(categoryEntity.getCreatedAt());

                    CategoryEntity parent = categoryEntity.getParent();
                    if (parent != null) {
                        categoryResponse.setParentId(parent.getId());
                        categoryResponse.setParentName(parent.getName());
                    } else {
                        categoryResponse.setParentId(null);
                        categoryResponse.setParentName(null);
                    }

                    return categoryResponse;
                });
//        Page<CategoryResponse> categoryResponses = categoryEntities
//                .map(categoryEntity -> {
//                    CategoryResponse categoryResponse = categoryConverter.toResponse(categoryEntity);
//
//                    CategoryEntity parent = categoryEntity.getParent();
//                    if (parent != null) {
//                        CategoryResponse parentResponse = new CategoryResponse(
//                                parent.getId(),
//                                parent.getName(),
//                                parent.getImageUrl(),
//                                parent.getStatus(),
//                                null,
//                                null
//                        );
//
//                        categoryResponse.setParent(parentResponse);
//                    }
//
//                    List<CategoryResponse> children = categoryEntity.getChildren() != null
//                            ? categoryEntity.getChildren().stream()
//                            .map(c -> new CategoryResponse(
//                                    c.getId(),
//                                    c.getName(),
//                                    c.getImageUrl(),
//                                    c.getStatus(),
//                                    null, // parent không map để tránh vòng lặp
//                                    null  // children của children nếu muốn, map thêm cẩn thận
//                            )).toList()
//                            : null;
//
//                    categoryResponse.setChildren(children);
//                    return categoryResponse;
//                });

        return categoryResponses;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        CategoryEntity categoryEntity = categoryConverter.toEntity(categoryRequest);
        categoryEntity.setId(null);

        if (categoryRequest.getParentId() != null) {
            CategoryEntity parentEntity = categoryRepository.findById(categoryRequest.getParentId()).get();
            if (parentEntity != null) {
                categoryEntity.setParent(parentEntity);
            } else {
                categoryEntity.setParent(null);
            }
        } else {
            categoryEntity.setParent(null);
        }

        CategoryResponse categoryResponse = categoryConverter.toResponse(categoryRepository.save(categoryEntity));

        return categoryResponse;
    }

    @Override
    public CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest) {
        CategoryEntity categoryEntity = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id: " + id));

        categoryEntity.setName(categoryRequest.getName());
        categoryEntity.setImageUrl(categoryRequest.getImageUrl());
        categoryEntity.setStatus(categoryRequest.getStatus());

        if (categoryRequest.getParentId() != null) {
            CategoryEntity parentEntity = categoryRepository.findById(categoryRequest.getParentId()).get();
            if (parentEntity != null) {
                categoryEntity.setParent(parentEntity);
            } else {
                categoryEntity.setParent(null);
            }
        } else {
            categoryEntity.setParent(null);
        }

        CategoryResponse categoryResponse = categoryConverter.toResponse(categoryRepository.save(categoryEntity));

        return categoryResponse;
    }

    @Override
    public List<CategoryResponse> getLeafCategories() {
        List<CategoryEntity> categoryEntities = categoryRepository.findLeafCategories();
        List<CategoryResponse> categoryResponses = categoryEntities.stream()
                .map(categoryConverter::toResponse)
                .toList();
        return categoryResponses;
    }

    @Override
    public List<CategoryResponse> getByShopId(Integer shopId) {
        List<CategoryEntity> categoryEntities = categoryRepository.findDistinctByShopId(shopId);
        return categoryEntities.stream().map(c -> {
            CategoryResponse categoryResponse = new CategoryResponse();
            categoryResponse.setId(c.getId());
            categoryResponse.setName(c.getName());
            return  categoryResponse;
        }).toList();
    }

    public CategoryDetailResponse getCategoryById(Integer id) {
        CategoryEntity category = categoryRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục id: " + id));

        boolean isParent = category.getParent() == null;

        List<CategoryEntity> displayCategories;

        if (isParent) {
            displayCategories = categoryRepository.findChildrenByParentId(category.getId());
        } else {
            displayCategories = new ArrayList<>(
                    categoryRepository.findRelatedByParentId(
                            category.getParent().getId(),
                            category.getId()
                    )
            );

            displayCategories.add(0, category);
        }

        List<CategoryItemResponse> categoryItems = displayCategories.stream()
                .map(item -> {
                    List<Integer> ids = getCategoryIdsForProductQuery(item);
                    Long totalProduct = productRepository.countActiveByCategoryIds(ids);

                    return CategoryItemResponse.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .imageUrl(item.getImageUrl())
                            .totalProduct(totalProduct)
                            .build();
                })
                .toList();

        return CategoryDetailResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .parentId(isParent ? null : category.getParent().getId())
                .parentName(isParent ? null : category.getParent().getName())
                .parentCategory(isParent)
                .sectionTitle(isParent ? "Danh mục con" : "Danh mục liên quan")
                .sectionDescription(isParent
                        ? "Chọn nhanh nhóm sản phẩm bạn muốn xem"
                        : "Các danh mục cùng nhóm với " + category.getName())
                .categories(categoryItems)
                .build();
    }

    public List<Integer> getCategoryIdsForProductQuery(CategoryEntity category) {
        if (category.getParent() == null) {
            List<CategoryEntity> children = categoryRepository.findChildrenByParentId(category.getId());

            if (children.isEmpty()) {
                return List.of(category.getId());
            }

            return children.stream()
                    .map(CategoryEntity::getId)
                    .toList();
        }

        return List.of(category.getId());
    }
}
