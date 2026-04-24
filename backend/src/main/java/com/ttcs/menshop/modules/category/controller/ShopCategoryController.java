package com.ttcs.menshop.modules.category.controller;

import com.ttcs.menshop.modules.category.dto.response.CategoryResponse;
import com.ttcs.menshop.modules.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop/categories")
public class ShopCategoryController {

    private final CategoryService categoryService;
    public ShopCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<?> getCategories() {
        List<CategoryResponse> categoryResponseList = categoryService.getLeafCategories();
        return ResponseEntity.ok().body(categoryResponseList);
    }
}
