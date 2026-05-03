package com.ttcs.menshop.modules.search.controller;

import com.ttcs.menshop.modules.search.service.SyncDataService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/sync")
public class SyncController {
    SyncDataService syncDataService;

    @PostMapping("/products")
    public ResponseEntity<String> syncProducts() {
        String result = syncDataService.syncAllProductsToElastic();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/vectors")
    public ResponseEntity<String> syncVectors() {
        String result = syncDataService.syncAllVectors();
        return ResponseEntity.ok(result);
    }
}
