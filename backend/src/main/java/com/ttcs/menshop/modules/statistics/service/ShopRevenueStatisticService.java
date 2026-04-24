package com.ttcs.menshop.modules.statistics.service;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import com.ttcs.menshop.modules.statistics.dto.RevenueChartItemResponse;
import com.ttcs.menshop.modules.statistics.dto.RevenueDetailItemResponse;
import com.ttcs.menshop.modules.statistics.dto.ShopRevenueStatisticResponse;
import com.ttcs.menshop.modules.statistics.repository.ShopRevenueStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShopRevenueStatisticService {

    private final AuthService authService;
    private final ShopRepository shopRepository;
    private final ShopRevenueStatisticRepository shopRevenueStatisticRepository;

    public ShopRevenueStatisticResponse getRevenueStatistic(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new RuntimeException("fromDate và toDate không được để trống");
        }
        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("fromDate không được lớn hơn toDate");
        }
        if (fromDate.plusDays(366).isBefore(toDate)) {
            throw new RuntimeException("Chỉ được thống kê tối đa 366 ngày");
        }

        UserEntity currentUser = authService.getCurrentUser();
        ShopEntity shop = shopRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        Integer shopId = shop.getId();

        ShopRevenueStatisticResponse response = new ShopRevenueStatisticResponse();

        response.setOverview(buildOverview(shopId, fromDate, toDate));
        List<RevenueChartItemResponse> chartData = buildChartData(shopId, fromDate, toDate);
        response.setChartData(chartData);

        List<RevenueDetailItemResponse> detailRows = chartData.stream()
                .map(item -> new RevenueDetailItemResponse(
                        item.getLabel(),
                        item.getTotalOrders(),
                        item.getDeliveredOrders(),
                        item.getCancelledOrders(),
                        item.getRevenue(),
                        calcCancelRate(item.getCancelledOrders(), item.getTotalOrders())
                ))
                .toList();
        response.setDetailRows(detailRows);

        response.setTopProducts(
                shopRevenueStatisticRepository.getTopRevenueProducts(shopId, fromDate, toDate, 5)
        );

        return response;
    }

    private ShopRevenueStatisticResponse.Overview buildOverview(Integer shopId, LocalDate fromDate, LocalDate toDate) {
        Object[] row = shopRevenueStatisticRepository.getOverviewRaw(shopId, fromDate, toDate);

        BigDecimal totalRevenue = toBigDecimal(row[0]);
        Integer totalOrders = toInt(row[1]);
        Integer deliveredOrders = toInt(row[2]);
        Integer cancelledOrders = toInt(row[3]);

        ShopRevenueStatisticResponse.Overview overview = new ShopRevenueStatisticResponse.Overview();
        overview.setTotalRevenue(totalRevenue);
        overview.setTotalOrders(totalOrders);
        overview.setDeliveredOrders(deliveredOrders);
        overview.setCancelledOrders(cancelledOrders);
        overview.setAverageOrderValue(calculateAverageOrderValue(totalRevenue, deliveredOrders));

        return overview;
    }

    private List<RevenueChartItemResponse> buildChartData(Integer shopId, LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rawRows = shopRevenueStatisticRepository.getRevenueChartRaw(shopId, fromDate, toDate);

        Map<LocalDate, RevenueChartItemResponse> dataMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (Object[] row : rawRows) {
            LocalDate date = extractLocalDateFromObject(row[0]);
            RevenueChartItemResponse item = new RevenueChartItemResponse(
                    date.format(formatter),
                    toInt(row[1]),
                    toInt(row[2]),
                    toInt(row[3]),
                    toBigDecimal(row[4])
            );
            dataMap.put(date, item);
        }

        List<RevenueChartItemResponse> result = new ArrayList<>();
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            RevenueChartItemResponse existing = dataMap.get(current);
            if (existing != null) {
                result.add(existing);
            } else {
                result.add(new RevenueChartItemResponse(
                        current.format(formatter),
                        0,
                        0,
                        0,
                        BigDecimal.ZERO
                ));
            }
            current = current.plusDays(1);
        }

        return result;
    }

    private BigDecimal calculateAverageOrderValue(BigDecimal totalRevenue, Integer deliveredOrders) {
        if (deliveredOrders == null || deliveredOrders == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(
                BigDecimal.valueOf(deliveredOrders),
                0,
                java.math.RoundingMode.HALF_UP
        );
    }

    private Integer calcCancelRate(Integer cancelledOrders, Integer totalOrders) {
        if (totalOrders == null || totalOrders == 0) return 0;
        return cancelledOrders * 100 / totalOrders;
    }

    private Integer toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        return 0;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bigDecimal) return bigDecimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return BigDecimal.ZERO;
    }

    private LocalDate extractLocalDateFromObject(Object value) {
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof java.sql.Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        return LocalDate.parse(String.valueOf(value));
    }
}
