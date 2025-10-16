package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
