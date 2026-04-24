package com.ttcs.menshop.modules.statistics.controller;

import com.ttcs.menshop.modules.statistics.dto.ShopRevenueStatisticResponse;
import com.ttcs.menshop.modules.statistics.service.ShopRevenueStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/shop/statistics/revenue")
@RequiredArgsConstructor
public class ShopRevenueStatisticController {

    private final ShopRevenueStatisticService shopRevenueStatisticService;

    @GetMapping
    public ShopRevenueStatisticResponse getRevenueStatistic(
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate
    ) {
        return shopRevenueStatisticService.getRevenueStatistic(fromDate, toDate);
    }
}
