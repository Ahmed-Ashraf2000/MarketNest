package com.marketnest.ecommerce.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for creating a new product review")
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    @Schema(
            description = "ID of the product being reviewed",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(
            description = "Star rating for the product. Must be between 1 (poor) and 5 (excellent)",
            example = "4",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1",
            maximum = "5"
    )
    private Integer rating;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Schema(
            description = "Short title summarizing the review",
            example = "Great product, highly recommended!",
            maxLength = 100,
            nullable = true
    )
    private String title;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Schema(
            description = "Detailed review comment describing the experience with the product",
            example = "The sound quality is exceptional and the battery life exceeds expectations. Comfortable for long listening sessions.",
            maxLength = 1000,
            nullable = true
    )
    private String comment;
}