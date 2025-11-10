package com.marketnest.ecommerce.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for updating an existing review")
public class UpdateReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(
            description = "Updated star rating for the product. Must be between 1 (poor) and 5 (excellent)",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1",
            maximum = "5"
    )
    private Integer rating;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Schema(
            description = "Updated review title",
            example = "Amazing product after extended use!",
            maxLength = 100,
            nullable = true
    )
    private String title;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    @Schema(
            description = "Updated review comment with new insights",
            example = "After using for 3 months, the quality remains excellent. Battery still lasts all day.",
            maxLength = 1000,
            nullable = true
    )
    private String comment;
}