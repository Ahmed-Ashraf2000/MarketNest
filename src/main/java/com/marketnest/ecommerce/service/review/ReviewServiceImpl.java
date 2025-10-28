package com.marketnest.ecommerce.service.review;

import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import com.marketnest.ecommerce.exception.*;
import com.marketnest.ecommerce.mapper.review.ReviewMapper;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.Review;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.OrderRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.ReviewRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        return reviewRepository.findByProductIdAndIsApprovedTrue(productId, pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        return reviewMapper.toResponse(review);
    }

    @Override
    public ReviewResponse createReview(Long productId, Long userId, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with id: " + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + userId));

        if (reviewRepository.existsByProductIdAndUser_UserId(productId, userId)) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        Review review = reviewMapper.toEntity(request);
        review.setProduct(product);
        review.setUser(user);

        boolean verifiedPurchase = isVerifiedPurchase(userId, productId);
        review.setVerifiedPurchase(verifiedPurchase);

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }


    @Override
    public ReviewResponse updateReview(Long reviewId, Long userId, UpdateReviewRequest request) {
        Review review = reviewRepository.findByIdAndUser_UserId(reviewId, userId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        reviewMapper.updateEntityFromDto(request, review);

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Override
    public void markReviewAsHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        reviewRepository.delete(review);
    }

    @Override
    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        review.setIsApproved(true);
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(updatedReview);
    }

    @Override
    public ReviewResponse rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        review.setIsApproved(false);
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(updatedReview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(reviewMapper::toResponse);
    }


    private boolean isVerifiedPurchase(Long userId, Long productId) {
        return orderRepository.existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn(
                userId,
                productId,
                List.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.SHIPPED)
        );
    }

}