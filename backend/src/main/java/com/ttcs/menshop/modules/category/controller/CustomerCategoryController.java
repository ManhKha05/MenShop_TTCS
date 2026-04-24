package com.ttcs.menshop.modules.category.controller;

import com.ttcs.menshop.modules.category.dto.response.CategoryDetailResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryResponse;
import com.ttcs.menshop.modules.category.service.CategoryService;
import com.ttcs.menshop.modules.product.dto.response.ProductCardResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CustomerCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    public CustomerCategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping("/parents")
    public ResponseEntity<?> getAllParents(){
        List<CategoryResponse> categoryResponseList = categoryService.getActiveParentCategories();
        return ResponseEntity.ok().body(categoryResponseList);
    }

    @GetMapping("/{id}")
    public CategoryDetailResponse getCategory(@PathVariable Integer id) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping("/{id}/products")
    public Page<ProductCardResponse> getProductsByCategory(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "popular") String sort,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) List<Integer> childCategoryIds
    ) {
        return productService.getProductsByCategory(
                id,
                page,
                size,
                sort,
                minPrice,
                maxPrice,
                rating,
                childCategoryIds
        );
    }
}
