package com.ttcs.menshop.modules.flash_sale.controller;

import com.ttcs.menshop.modules.flash_sale.dto.response.SaleHomeResponse;
import com.ttcs.menshop.modules.flash_sale.service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flash-sale")
public class UserSaleController {

    private final SaleService saleService;

    public UserSaleController(SaleService saleService) {
        this.saleService = saleService;
    }


    @GetMapping("/active")
    public ResponseEntity<?> getActiveFlashSale() {
        SaleHomeResponse res = saleService.getSaleHome();
        return ResponseEntity.ok(res);
    }
}
