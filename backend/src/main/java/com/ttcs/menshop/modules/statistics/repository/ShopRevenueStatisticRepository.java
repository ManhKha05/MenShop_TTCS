package com.ttcs.menshop.modules.statistics.repository;

import com.ttcs.menshop.modules.statistics.dto.TopRevenueProductResponse;

import java.time.LocalDate;
import java.util.List;

public interface ShopRevenueStatisticRepository {
    Object[] getOverviewRaw(Integer shopId, LocalDate fromDate, LocalDate toDate);
    List<Object[]> getRevenueChartRaw(Integer shopId, LocalDate fromDate, LocalDate toDate);
    List<TopRevenueProductResponse> getTopRevenueProducts(Integer shopId, LocalDate fromDate, LocalDate toDate, int limit);
}
