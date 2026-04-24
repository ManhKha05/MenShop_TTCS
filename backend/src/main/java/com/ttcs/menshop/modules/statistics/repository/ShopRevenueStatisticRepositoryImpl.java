package com.ttcs.menshop.modules.statistics.repository;

import com.ttcs.menshop.modules.statistics.dto.TopRevenueProductResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShopRevenueStatisticRepositoryImpl implements ShopRevenueStatisticRepository {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Object[] getOverviewRaw(Integer shopId, LocalDate fromDate, LocalDate toDate) {
        return (Object[]) entityManager.createNativeQuery("""
        SELECT
            COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN oi.unit_price * oi.quantity ELSE 0 END), 0) AS totalRevenue,
            COUNT(DISTINCT o.id) AS totalOrders,
            COUNT(DISTINCT CASE WHEN o.status = 'DELIVERED' THEN o.id END) AS deliveredOrders,
            COUNT(DISTINCT CASE WHEN o.status = 'CANCELLED' THEN o.id END) AS cancelledOrders
        FROM order_item oi
        JOIN `orders` o ON o.id = oi.order_id
        WHERE oi.shop_id = ?1
          AND o.created_at >= ?2
          AND o.created_at < ?3
    """)
                .setParameter(1, shopId)
                .setParameter(2, fromDate.atStartOfDay())
                .setParameter(3, toDate.plusDays(1).atStartOfDay())
                .getSingleResult();
    }
    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getRevenueChartRaw(Integer shopId, LocalDate fromDate, LocalDate toDate) {
        return entityManager.createNativeQuery("""
            SELECT
                DATE(o.created_at) AS orderDate,
                COUNT(DISTINCT o.id) AS totalOrders,
                COUNT(DISTINCT CASE WHEN o.status = 'DELIVERED' THEN o.id END) AS deliveredOrders,
                COUNT(DISTINCT CASE WHEN o.status = 'CANCELLED' THEN o.id END) AS cancelledOrders,
                COALESCE(SUM(CASE WHEN o.status = 'DELIVERED' THEN oi.unit_price * oi.quantity ELSE 0 END), 0) AS revenue
            FROM order_item oi
            JOIN orders o ON o.id = oi.order_id
            WHERE oi.shop_id = :shopId
              AND o.created_at >= :start
              AND o.created_at < :end
            GROUP BY DATE(o.created_at)
            ORDER BY DATE(o.created_at) ASC
        """)
                .setParameter("shopId", shopId)
                .setParameter("start", fromDate.atStartOfDay())
                .setParameter("end", toDate.plusDays(1).atStartOfDay())
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TopRevenueProductResponse> getTopRevenueProducts(
            Integer shopId,
            LocalDate fromDate,
            LocalDate toDate,
            int limit
    ) {
        List<Object[]> rows = entityManager.createNativeQuery("""
            SELECT
                p.id AS productId,
                p.name AS productName,
                (
                    SELECT pi.image_url
                    FROM product_image pi
                    WHERE pi.product_id = p.id
                    ORDER BY pi.id ASC
                    LIMIT 1
                ) AS image,
                COALESCE(SUM(oi.quantity), 0) AS soldCount,
                COALESCE(SUM(oi.unit_price * oi.quantity), 0) AS revenue
            FROM order_item oi
            JOIN orders o ON o.id = oi.order_id
            JOIN product_variant pv ON pv.id = oi.variant_id
            JOIN product p ON p.id = pv.product_id
            WHERE p.shop_id = :shopId
              AND o.status = 'DELIVERED'
              AND o.created_at >= :start
              AND o.created_at < :end
            GROUP BY p.id, p.name
            ORDER BY revenue DESC
            LIMIT :limitValue
        """)
                .setParameter("shopId", shopId)
                .setParameter("start", fromDate.atStartOfDay())
                .setParameter("end", toDate.plusDays(1).atStartOfDay())
                .setParameter("limitValue", limit)
                .getResultList();

        List<TopRevenueProductResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new TopRevenueProductResponse(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    row[2] != null ? String.valueOf(row[2]) : null,
                    ((Number) row[3]).intValue(),
                    toBigDecimal(row[4])
            ));
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bigDecimal) return bigDecimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return BigDecimal.ZERO;
    }
}
