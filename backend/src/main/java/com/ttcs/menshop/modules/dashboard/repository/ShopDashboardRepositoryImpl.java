package com.ttcs.menshop.modules.dashboard.repository;

import com.ttcs.menshop.modules.dashboard.dto.MonthlyStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.OrderStatusStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.RecentShopOrderResponse;
import com.ttcs.menshop.modules.dashboard.dto.TopProductResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShopDashboardRepositoryImpl implements ShopDashboardRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public BigDecimal getRevenueThisMonth(Integer shopId) {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay.plusMonths(1).atStartOfDay();

        BigDecimal result = entityManager.createQuery("""
            select coalesce(sum(oi.unitPrice * oi.quantity), 0)
            from OrderItemEntity oi
            join oi.order o
            where oi.shop.id = :shopId
              and o.status = 'DELIVERED'
              and o.createdAt >= :start and o.createdAt < :end
        """, BigDecimal.class)
                .setParameter("shopId", shopId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public Integer getOrdersThisMonth(Integer shopId) {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        Long count = entityManager.createQuery("""
            select count(distinct o.id)
            from OrderItemEntity oi
            join oi.order o
            where oi.shop.id = :shopId
              and o.createdAt >= :start
        """, Long.class)
                .setParameter("shopId", shopId)
                .setParameter("start", firstDay.atStartOfDay())
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getPendingOrders(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(distinct o.id)
            from OrderItemEntity oi
            join oi.order o
            where oi.shop.id = :shopId
              and o.status = 'PENDING'
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getActiveProducts(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(p.id)
            from ProductEntity p
            where p.shop.id = :shopId
              and p.status = 'ACTIVE'
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getOutOfStockProducts(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(distinct p.id)
            from ProductEntity p
            where p.shop.id = :shopId
              and p.status = 'ACTIVE'
              and not exists (
                  select 1
                  from ProductVariantEntity v
                  where v.product.id = p.id and v.stock > 0
              )
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getLowStockProducts(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(distinct p.id)
            from ProductEntity p
            join p.variants v
            where p.shop.id = :shopId
              and p.status = 'ACTIVE'
              and v.stock > 0
              and v.stock <= 5
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getPendingProducts(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(p.id)
            from ProductEntity p
            where p.shop.id = :shopId
              and p.status = 'PENDING'
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public BigDecimal getAverageRating(Integer shopId) {
        Double result = entityManager.createQuery("""
        select coalesce(avg(r.rating), 0)
        from ReviewEntity r
        where r.product.shop.id = :shopId
    """, Double.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return result == null ? BigDecimal.ZERO : BigDecimal.valueOf(result);
    }

    @Override
    public Integer getTotalReviews(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(r.id)
            from ReviewEntity r
            where r.product.shop.id = :shopId
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getLowRatingReviews(Integer shopId) {
        Long count = entityManager.createQuery("""
            select count(r.id)
            from ReviewEntity r
            where r.product.shop.id = :shopId
              and r.rating <= 2
        """, Long.class)
                .setParameter("shopId", shopId)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public List<MonthlyStatResponse> getMonthlyStats(Integer shopId, int months) {
        List<MonthlyStatResponse> result = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDateTime start = month.atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atStartOfDay();

            Long orders = entityManager.createQuery("""
                select count(distinct o.id)
                from OrderItemEntity oi
                join oi.order o
                where oi.shop.id = :shopId
                  and o.createdAt >= :start and o.createdAt < :end
            """, Long.class)
                    .setParameter("shopId", shopId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();

            BigDecimal revenue = entityManager.createQuery("""
                select coalesce(sum(oi.unitPrice * oi.quantity), 0)
                from OrderItemEntity oi
                join oi.order o
                where oi.shop.id = :shopId
                  and o.status = 'DELIVERED'
                  and o.createdAt >= :start and o.createdAt < :end
            """, BigDecimal.class)
                    .setParameter("shopId", shopId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();

            result.add(new MonthlyStatResponse(
                    "Tháng " + month.getMonthValue(),
                    orders.intValue(),
                    revenue == null ? BigDecimal.ZERO : revenue
            ));
        }

        return result;
    }

    @Override
    public List<OrderStatusStatResponse> getOrderStatusStats(Integer shopId) {
        List<Object[]> rows = entityManager.createQuery("""
            select o.status, count(distinct o.id)
            from OrderItemEntity oi
            join oi.order o
            where oi.shop.id = :shopId
            group by o.status
        """, Object[].class)
                .setParameter("shopId", shopId)
                .getResultList();

        List<OrderStatusStatResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new OrderStatusStatResponse(
                    String.valueOf(row[0]),
                    ((Long) row[1]).intValue()
            ));
        }
        return result;
    }

    @Override
    public List<TopProductResponse> getTopProducts(Integer shopId, int limit) {
        List<Object[]> rows = entityManager.createQuery("""
            select p.id,
                   p.name,
                   (select pi.imageUrl from ProductImageEntity pi where pi.product.id = p.id order by pi.id asc limit 1),
                   coalesce(sum(oi.quantity), 0),
                   coalesce(sum(v.stock), 0),
                   coalesce(sum(oi.unitPrice * oi.quantity), 0)
            from OrderItemEntity oi
            join oi.variant v
            join v.product p
            join oi.order o
            where p.shop.id = :shopId
              and o.status = 'DELIVERED'
            group by p.id, p.name
            order by coalesce(sum(oi.quantity), 0) desc
        """, Object[].class)
                .setParameter("shopId", shopId)
                .setMaxResults(limit)
                .getResultList();

        List<TopProductResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new TopProductResponse(
                    (Integer) row[0],
                    (String) row[1],
                    row[2] != null ? String.valueOf(row[2]) : null,
                    ((Long) row[3]).intValue(),
                    row[4] == null ? 0 : ((Long) row[4]).intValue(),
                    row[5] == null ? BigDecimal.ZERO : (BigDecimal) row[5]
            ));
        }

        return result;
    }

    @Override
    public List<RecentShopOrderResponse> getRecentOrders(Integer shopId, int limit) {
        List<Object[]> rows = entityManager.createQuery("""
            select distinct o.id, o.code, o.receiverName, o.finalTotal, o.status, o.createdAt
            from OrderItemEntity oi
            join oi.order o
            where oi.shop.id = :shopId
            order by o.createdAt desc
        """, Object[].class)
                .setParameter("shopId", shopId)
                .setMaxResults(limit)
                .getResultList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<RecentShopOrderResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new RecentShopOrderResponse(
                    (Integer) row[0],
                    (String) row[1],
                    (String) row[2],
                    (BigDecimal) row[3],
                    String.valueOf(row[4]),
                    row[5] == null ? "" : ((LocalDateTime) row[5]).format(formatter)
            ));
        }

        return result;
    }

}
