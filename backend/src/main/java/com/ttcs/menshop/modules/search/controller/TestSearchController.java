package com.ttcs.menshop.modules.search.controller;

import com.ttcs.menshop.modules.search.document.ProductDocument;
import com.ttcs.menshop.modules.search.service.TestSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestSearchController {

    private final TestSearchService testSearchService;

    @GetMapping("/search")
    public ResponseEntity<?> testSearchStrategies(
            @RequestParam("q") String keyword,
            @RequestParam(value = "strategy", defaultValue = "HYBRID") String strategy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        List<ProductDocument> results = testSearchService.searchProducts(keyword, strategy, page, size);

        return ResponseEntity.ok(results);
    }
}