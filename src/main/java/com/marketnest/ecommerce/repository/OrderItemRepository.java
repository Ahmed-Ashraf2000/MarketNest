package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.dto.analytics.TopSellingProductDto;
import com.marketnest.ecommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT new com.marketnest.ecommerce.dto.analytics.TopSellingProductDto(" +
           "p.id, p.name, p.sku, SUM(oi.quantity), SUM(oi.totalPrice), " +
           "(SELECT pi.url FROM ProductImage pi WHERE pi.product.id = p.id ORDER BY pi.id LIMIT 1)) " +
           "FROM OrderItem oi JOIN oi.product p " +
           "WHERE oi.order.status != 'CANCELLED' " +
           "GROUP BY p.id, p.name, p.sku ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingProductDto> findTopSellingProducts(@Param("limit") int limit);
}
