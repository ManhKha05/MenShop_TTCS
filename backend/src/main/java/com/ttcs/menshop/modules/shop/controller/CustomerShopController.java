package com.ttcs.menshop.modules.shop.controller;

import com.ttcs.menshop.modules.category.service.CategoryService;
import com.ttcs.menshop.modules.product.dto.response.ProductCardResponse;
import com.ttcs.menshop.modules.product.service.ProductService;
import com.ttcs.menshop.modules.shop.dto.request.ShopRequest;
import com.ttcs.menshop.modules.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shops")
public class CustomerShopController {

    private final ShopService shopService;
    private final CategoryService categoryService;
    private final ProductService productService;

    public CustomerShopController(ShopService shopService, CategoryService categoryService, ProductService productService) {
        this.shopService = shopService;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ShopRequest shopRequest) {
        shopService.createShop(shopRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("Message", "Đăng ký mở shop thành công!"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShop(@PathVariable Integer id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping("/{id}/categories")
    public ResponseEntity<?> getShopCategories(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getByShopId(id));
    }

    @GetMapping("/{id}/best-selling-products")
    public ResponseEntity<?> getBestSellingProducts(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getBestSellingProductsByShop(id));
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> getShopProducts(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String priceOrder
    ) {
        Page<ProductCardResponse> res = productService.getProductsByShop(id, page, size, category, sort, priceOrder);
        return ResponseEntity.ok(res);
    }


}
