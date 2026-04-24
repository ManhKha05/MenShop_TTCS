package com.ttcs.menshop.modules.category.controller;

import com.ttcs.menshop.modules.category.dto.request.CategoryRequest;
import com.ttcs.menshop.modules.category.dto.response.CategoryResponse;
import com.ttcs.menshop.modules.category.dto.response.CategoryStatsResponse;
import com.ttcs.menshop.modules.category.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?>  getStats() {
        CategoryStatsResponse categoryStats = categoryService.getCategoryStats();
        return ResponseEntity.ok(categoryStats);
    }

    @GetMapping("/leaf")
    public ResponseEntity<?>  getLeafs() {
        List<CategoryResponse> categoryResponseList = categoryService.getLeafCategories();
        return ResponseEntity.ok().body(categoryResponseList);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer parentId,
            @RequestParam (required = false) String status
    ) {
        Page<CategoryResponse> categories = categoryService.searchCategories(keyword, parentId, status, page, size);
        return ResponseEntity.ok(categories);
    }



    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.createCategory(categoryRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.updateCategory(id, categoryRequest);
        return ResponseEntity.ok(response);
    }
}
