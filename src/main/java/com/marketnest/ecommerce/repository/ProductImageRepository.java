package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    List<ProductImage> findByProductIdAndIsActiveTrue(Long productId);
}
