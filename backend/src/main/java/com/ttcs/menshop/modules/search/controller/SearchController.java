package com.ttcs.menshop.modules.search.controller;

import com.ttcs.menshop.modules.search.document.ProductDocument;
import com.ttcs.menshop.modules.search.service.SmartSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin("*")
public class SearchController {

    SmartSearchService smartSearchService;

    @GetMapping
    public ResponseEntity<Page<ProductDocument>> searchProducts(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {

        if (keyword.trim().isEmpty()) {
            return ResponseEntity.ok(Page.empty());
        }

        if (size > 48) {
            size = 48;
        }

        long startTime = System.currentTimeMillis();

        Page<ProductDocument> results = smartSearchService.searchSmart(keyword, page, size);

        long endTime = System.currentTimeMillis();
        System.out.println("Search: '" + keyword + "' | Trả về: " + results.getNumberOfElements() + " SP | Tốc độ: " + (endTime - startTime) + "ms");

        return ResponseEntity.ok(results);
    }
}