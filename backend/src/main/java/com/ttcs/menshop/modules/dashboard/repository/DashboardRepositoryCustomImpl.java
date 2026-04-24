package com.ttcs.menshop.modules.dashboard.repository;

import com.ttcs.menshop.modules.dashboard.dto.MonthlyStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.OrderStatusStatResponse;
import com.ttcs.menshop.modules.dashboard.dto.TopShopResponse;
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
public class DashboardRepositoryCustomImpl implements DashboardRepositoryCustom {
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public BigDecimal getTotalRevenue() {
        BigDecimal result = (BigDecimal) entityManager.createQuery("""
            select coalesce(sum(o.finalTotal), 0)
            from OrderEntity o
            where o.status = 'DELIVERED'
        """).getSingleResult();
        return result == null ? BigDecimal.ZERO : result;
    }

    @Override
    public Integer getTotalOrders() {
        Long count = entityManager.createQuery("""
            select count(o.id) from OrderEntity o
        """, Long.class).getSingleResult();
        return count.intValue();
    }

    @Override
    public Integer getOrdersToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Long count = entityManager.createQuery("""
            select count(o.id)
            from OrderEntity o
            where o.createdAt >= :start and o.createdAt < :end
        """, Long.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getTotalUsers() {
        Long count = entityManager.createQuery("""
            select count(u.id) from UserEntity u
        """, Long.class).getSingleResult();
        return count.intValue();
    }

    @Override
    public Integer getNewUsersThisMonth() {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);

        Long count = entityManager.createQuery("""
            select count(u.id)
            from UserEntity u
            where u.createdAt >= :start
        """, Long.class)
                .setParameter("start", firstDay.atStartOfDay())
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getActiveShops() {
        Long count = entityManager.createQuery("""
            select count(s.id)
            from ShopEntity s
            where s.status = 'ACTIVE'
        """, Long.class).getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getPendingShops() {
        Long count = entityManager.createQuery("""
            select count(s.id)
            from ShopEntity s
            where s.status = 'PENDING'
        """, Long.class).getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getPendingProducts() {
        Long count = entityManager.createQuery("""
            select count(p.id)
            from ProductEntity p
            where p.status = 'PENDING'
        """, Long.class).getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getOutOfStockProducts() {
        Long count = entityManager.createQuery("""
            select count(distinct p.id)
            from ProductEntity p
            where p.status = 'ACTIVE'
              and not exists (
                  select 1 from ProductVariantEntity v
                  where v.product.id = p.id and v.stock > 0
              )
        """, Long.class).getSingleResult();

        return count.intValue();
    }

    @Override
    public Integer getCancelledOrdersThisMonth() {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);

        Long count = entityManager.createQuery("""
            select count(o.id)
            from OrderEntity o
            where o.status = 'CANCELLED'
              and o.createdAt >= :start
        """, Long.class)
                .setParameter("start", firstDay.atStartOfDay())
                .getSingleResult();

        return count.intValue();
    }

    @Override
    public List<MonthlyStatResponse> getMonthlyStats(int months) {
        List<MonthlyStatResponse> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDateTime start = month.atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atStartOfDay();

            Long orders = entityManager.createQuery("""
                select count(o.id)
                from OrderEntity o
                where o.createdAt >= :start and o.createdAt < :end
            """, Long.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();

            BigDecimal revenue = entityManager.createQuery("""
                select coalesce(sum(o.finalTotal), 0)
                from OrderEntity o
                where o.status = 'DELIVERED'
                  and o.createdAt >= :start and o.createdAt < :end
            """, BigDecimal.class)
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
    public List<OrderStatusStatResponse> getOrderStatusStats() {
        List<Object[]> rows = entityManager.createQuery("""
            select o.status, count(o.id)
            from OrderEntity o
            group by o.status
        """, Object[].class).getResultList();

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
    public List<TopShopResponse> getTopShops(int limit) {
        List<Object[]> rows = entityManager.createQuery("""
            select s.id, s.name, count(distinct o.id), coalesce(sum(oi.unitPrice * oi.quantity), 0)
            from OrderItemEntity oi
            join oi.order o
            join oi.shop s
            where o.status = 'DELIVERED'
            group by s.id, s.name
            order by coalesce(sum(oi.unitPrice * oi.quantity), 0) desc
        """, Object[].class)
                .setMaxResults(limit)
                .getResultList();

        List<TopShopResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new TopShopResponse(
                    (Integer) row[0],
                    (String) row[1],
                    ((Long) row[2]).intValue(),
                    (BigDecimal) row[3]
            ));
        }
        return result;
    }
}
