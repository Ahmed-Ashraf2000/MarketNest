package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.review.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Review Management", description = "APIs for managing product reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @Operation(summary = "Get product reviews",
            description = "Retrieves reviews for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    })
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    @Operation(summary = "Get review details",
            description = "Retrieves details of a specific review by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Review details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewDetails(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @Operation(summary = "Add a review", description = "Adds a new review for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review added successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Update a review", description = "Updates an existing review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
    }

    @Operation(summary = "Delete a review", description = "Deletes a review by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark review as helpful", description = "Marks a review as helpful.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Review marked as helpful successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PostMapping("/reviews/{reviewId}/helpful")
    public ResponseEntity<Void> markReviewAsHelpful(@PathVariable Long reviewId) {
        reviewService.markReviewAsHelpful(reviewId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a review by admin",
            description = "Deletes a review by its ID (admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @DeleteMapping("/admin/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReviewByAdmin(@PathVariable Long reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Approve a review", description = "Approves a review (admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review approved successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PatchMapping("/admin/reviews/{reviewId}/approve")
    public ResponseEntity<ReviewResponse> approveReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.approveReview(reviewId));
    }

    @Operation(summary = "Reject a review", description = "Rejects a review (admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review rejected successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    @PatchMapping("/admin/reviews/{reviewId}/reject")
    public ResponseEntity<ReviewResponse> rejectReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.rejectReview(reviewId));
    }

    @Operation(summary = "Get all reviews", description = "Retrieves all reviews (admin only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    })
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