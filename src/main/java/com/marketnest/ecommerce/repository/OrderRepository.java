package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser_UserId(Long userId, Pageable pageable);

    Optional<Order> findByUser_UserIdAndId(Long id, Long userId);

    boolean existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn(
            Long userId,
            Long productId,
            List<Order.OrderStatus> statuses
    );

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT AVG(o.total) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal calculateAverageOrderValue();

    Long countByStatus(Order.OrderStatus status);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderDate >= :start AND o.orderDate < :end AND o.status != 'CANCELLED'")
    BigDecimal calculateRevenueForPeriod(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :start AND o.orderDate < :end")
    Long countOrdersForPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT o.user.userId) FROM Order o")
    Long countDistinctCustomers();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> findOrderCountByStatus();

    @Query(value = "SELECT DATE(o.order_date) as date, COUNT(o.id) as orderCount, " +
                   "SUM(o.total) as totalSales, AVG(o.total) as averageOrderValue " +
                   "FROM orders o WHERE o.order_date >= :start AND o.order_date < :end " +
                   "AND o.status != 'CANCELLED' GROUP BY DATE(o.order_date) " +
                   "ORDER BY DATE(o.order_date)", nativeQuery = true)
    List<Object[]> findSalesStatisticsNative(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE(o.order_date) as date, SUM(o.total) as revenue, " +
                   "SUM(o.total - o.discount) as profit, COUNT(o.id) as orderCount " +
                   "FROM orders o WHERE o.order_date >= :start AND o.order_date < :end " +
                   "AND o.status != 'CANCELLED' GROUP BY DATE(o.order_date) " +
                   "ORDER BY DATE(o.order_date)", nativeQuery = true)
    List<Object[]> findRevenueReportNative(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

}