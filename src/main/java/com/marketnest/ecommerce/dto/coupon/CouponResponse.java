package com.marketnest.ecommerce.dto.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for complete coupon details in responses")
public class CouponResponse {

    @Schema(description = "Unique identifier of the coupon", example = "1")
    private Long id;

    @Schema(description = "Unique coupon code that customers enter at checkout",
            example = "SUMMER2025")
    private String code;

    @Schema(description = "Description explaining the coupon's purpose and terms",
            example = "Summer sale - 20% off on all electronics", nullable = true)
    private String description;

    @Schema(
            description = "Type of discount applied by this coupon",
            example = "PERCENTAGE",
            allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"}
    )
    private Coupon.DiscountType discountType;

    @Schema(description = "Discount value - percentage (0-100) or fixed amount based on discount type",
            example = "20.00")
    private BigDecimal discountValue;

    @Schema(description = "Minimum purchase amount required to use this coupon", example = "50.00",
            nullable = true)
    private BigDecimal minPurchaseAmount;

    @Schema(description = "Maximum discount amount that can be applied (caps percentage discounts)",
            example = "100.00", nullable = true)
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Total number of times this coupon can be used across all users",
            example = "1000", nullable = true)
    private Integer usageLimit;

    @Schema(description = "Number of times this coupon has been used so far", example = "150")
    private Integer usageCount;

    @Schema(description = "Maximum number of times a single user can use this coupon",
            example = "1", nullable = true)
    private Integer perUserLimit;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date and time when the coupon becomes active and usable",
            example = "2025-06-01T00:00:00")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Date and time when the coupon expires and can no longer be used",
            example = "2025-08-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Flag indicating if the coupon is currently active and available for use",
            example = "true")
    private Boolean isActive;

    @Schema(description = "Set of category IDs that this coupon applies to. Empty means applies to all categories",
            nullable = true)
    private Set<Long> applicableCategories;

    @Schema(description = "Set of product IDs that this coupon applies to. Empty means applies to all products",
            nullable = true)
    private Set<Long> applicableProducts;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the coupon was created", example = "2025-05-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the coupon was last updated",
            example = "2025-05-20T14:20:30")
    private LocalDateTime updatedAt;
}