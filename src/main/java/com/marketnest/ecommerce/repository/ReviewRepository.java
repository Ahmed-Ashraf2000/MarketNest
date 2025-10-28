package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndIsApprovedTrue(Long productId, Pageable pageable);

    Page<Review> findByUser_UserId(Long userId, Pageable pageable);

    Optional<Review> findByIdAndUser_UserId(Long id, Long userId);

    boolean existsByProductIdAndUser_UserId(Long productId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
    Long getTotalReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating = :rating AND r.isApproved = true")
    Long countByProductIdAndRating(@Param("productId") Long productId,
                                   @Param("rating") Integer rating);

    List<Review> findTop5ByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId);
}