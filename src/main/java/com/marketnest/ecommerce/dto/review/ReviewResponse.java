package com.marketnest.ecommerce.dto.review;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object for product review details in responses")
public class ReviewResponse {

    @Schema(description = "Unique identifier of the review", example = "1")
    private Long id;

    @Schema(description = "ID of the product being reviewed", example = "101")
    private Long productId;

    @Schema(description = "Name of the product being reviewed",
            example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "ID of the user who wrote the review", example = "5")
    private Long userId;

    @Schema(description = "Name of the user who wrote the review", example = "John Doe")
    private String userName;

    @Schema(description = "Star rating given (1-5)", example = "4", minimum = "1", maximum = "5")
    private Integer rating;

    @Schema(description = "Review title", example = "Great product, highly recommended!",
            nullable = true)
    private String title;

    @Schema(description = "Detailed review comment",
            example = "The sound quality is exceptional and the battery life exceeds expectations",
            nullable = true)
    private String comment;

    @Schema(description = "Flag indicating if the reviewer purchased the product from this store",
            example = "true")
    private Boolean verifiedPurchase;

    @Schema(description = "Number of users who found this review helpful", example = "12")
    private Integer helpfulCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the review was created", example = "2025-01-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the review was last updated",
            example = "2025-01-16T14:20:30", nullable = true)
    private LocalDateTime updatedAt;

    @Schema(description = "Flag indicating if the review has been approved by admin for public display",
            example = "true")
    private Boolean isApproved;
}