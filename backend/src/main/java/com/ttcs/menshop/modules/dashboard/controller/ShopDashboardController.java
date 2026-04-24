package com.ttcs.menshop.modules.dashboard.controller;

import com.ttcs.menshop.modules.dashboard.dto.ShopDashboardResponse;
import com.ttcs.menshop.modules.dashboard.service.ShopDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop/dashboard")
@RequiredArgsConstructor
public class ShopDashboardController {

    private final ShopDashboardService shopDashboardService;

    @GetMapping
    public ShopDashboardResponse getDashboard() {
        return shopDashboardService.getDashboard();
    }
}
