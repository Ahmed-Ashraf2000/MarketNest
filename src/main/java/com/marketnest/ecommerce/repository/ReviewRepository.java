package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndIsApprovedTrue(Long productId, Pageable pageable);


    Optional<Review> findByIdAndUser_UserId(Long id, Long userId);

    boolean existsByProductIdAndUser_UserId(Long productId, Long userId);

}