package com.marketnest.ecommerce.service.review;

import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<ReviewResponse> getAllReviews(Pageable pageable);

    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    ReviewResponse getReviewById(Long reviewId);

    ReviewResponse createReview(Long productId, Long userId, CreateReviewRequest request);

    ReviewResponse updateReview(Long reviewId, Long userId, UpdateReviewRequest request);

    void deleteReview(Long reviewId, Long userId);

    void markReviewAsHelpful(Long reviewId);

    void deleteReviewByAdmin(Long reviewId);

    ReviewResponse approveReview(Long reviewId);

    ReviewResponse rejectReview(Long reviewId);
}