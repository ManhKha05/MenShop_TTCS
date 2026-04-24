package com.ttcs.menshop.modules.flash_sale.controller;

import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRequest;
import com.ttcs.menshop.modules.flash_sale.dto.response.AdminSaleProductResponse;
import com.ttcs.menshop.modules.flash_sale.dto.response.SaleResponse;
import com.ttcs.menshop.modules.flash_sale.dto.response.SaleStatsResponse;
import com.ttcs.menshop.modules.flash_sale.service.SaleService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/flash-sales")
public class AdminSaleController {

    private final SaleService saleService;

    public AdminSaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        SaleStatsResponse saleStatsResponse = saleService.getSaleStats();
        return new ResponseEntity<>(saleStatsResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        Page<SaleResponse> all = saleService.getAll(keyword, status, page, size);
        return ResponseEntity.ok(all);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SaleRequest saleRequest) {
        saleService.createSale(saleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Sale created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody SaleRequest saleRequest) {
        saleService.updateSale(id, saleRequest);
        return ResponseEntity.ok(Map.of("message", "Sale updated successfully"));
    }

    @PatchMapping("/{id}/end")
    public ResponseEntity<?> end(@PathVariable Integer id) {
        saleService.endSale(id);
        return ResponseEntity.ok(Map.of("message", "Sale ended successfully"));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable Integer id) {
        saleService.disableSale(id);
        return ResponseEntity.ok(Map.of("message", "Sale disabled successfully"));
    }

    @GetMapping("/{flashSaleId}/products")
    public ResponseEntity<?> getById(
            @PathVariable Integer flashSaleId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        Page<AdminSaleProductResponse> res =
                saleService.getProductsOfFlashSale(flashSaleId, page, size);
        return ResponseEntity.ok(res);
    }
}
