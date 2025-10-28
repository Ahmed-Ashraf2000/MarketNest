package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewDetails(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        Long userId = extractUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(productId, userId, request));
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Void> markReviewAsHelpful(@PathVariable Long reviewId) {
        reviewService.markReviewAsHelpful(reviewId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReviewByAdmin(@PathVariable Long reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/reviews/{reviewId}/approve")
    public ResponseEntity<ReviewResponse> approveReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.approveReview(reviewId));
    }

    @PatchMapping("/admin/reviews/{reviewId}/reject")
    public ResponseEntity<ReviewResponse> rejectReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.rejectReview(reviewId));
    }

    @GetMapping("/admin/reviews")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("The user not found"));

        return user.getUserId();
    }
}