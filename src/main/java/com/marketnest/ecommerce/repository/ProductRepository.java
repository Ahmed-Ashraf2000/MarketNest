package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.dto.analytics.LowStockProductDto;
import com.marketnest.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    Page<Product> findByIsActiveTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String description, Pageable pageable);

    Optional<Product> findBySlug(String slug);

    Page<Product> findByCategoryIdAndIdNot(Long categoryId, Long productId, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrueAndIdNot(Long categoryId, Long productId,
                                                          Pageable pageable);

    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Product> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<Product> findByIsFeaturedTrue(Pageable pageable);

    Page<Product> findByIsFeaturedTrueAndIsActiveTrue(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.isActive = true")
    Long countLowStockProducts();

    @Query("SELECT new com.marketnest.ecommerce.dto.analytics.LowStockProductDto(" +
           "p.id, p.name, p.sku, p.stockQuantity, p.lowStockThreshold, " +
           "(SELECT pi.url FROM ProductImage pi WHERE pi.product.id = p.id ORDER BY pi.id LIMIT 1)) " +
           "FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.isActive = true " +
           "ORDER BY (p.stockQuantity - p.lowStockThreshold)")
    List<LowStockProductDto> findLowStockProducts();
}
