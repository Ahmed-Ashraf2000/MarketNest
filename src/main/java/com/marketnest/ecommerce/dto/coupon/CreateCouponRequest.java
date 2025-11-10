package com.marketnest.ecommerce.dto.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for creating a new coupon (admin operation)")
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$",
            message = "Coupon code must contain only uppercase letters, numbers, hyphens, and underscores")
    @Schema(
            description = "Unique coupon code. Must be uppercase letters, numbers, hyphens, or underscores only",
            example = "SUMMER2025",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 50,
            pattern = "^[A-Z0-9_-]+$"
    )
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(
            description = "Description explaining the coupon's purpose, terms, and applicable items",
            example = "Summer sale - 20% off on all electronics. Valid until August 31st",
            maxLength = 500,
            nullable = true
    )
    private String description;

    @NotNull(message = "Discount type is required")
    @Schema(
            description = "Type of discount: PERCENTAGE (0-100) or FIXED_AMOUNT (currency value)",
            example = "PERCENTAGE",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"}
    )
    private Coupon.DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @Schema(
            description = "Discount value - percentage (0-100) for PERCENTAGE type or amount for FIXED_AMOUNT type",
            example = "20.00",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Minimum purchase amount must be non-negative")
    @Schema(
            description = "Minimum cart/order total required to use this coupon. Set to 0 or null for no minimum",
            example = "50.00",
            minimum = "0.00",
            nullable = true
    )
    private BigDecimal minPurchaseAmount;

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    @Schema(
            description = "Maximum discount amount (caps percentage discounts). Useful to limit large percentage discounts",
            example = "100.00",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    @Schema(
            description = "Total number of times this coupon can be used across all users. Null for unlimited usage",
            example = "1000",
            minimum = "1",
            nullable = true
    )
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    @Schema(
            description = "Maximum times a single user can use this coupon. Null for unlimited per-user usage",
            example = "1",
            minimum = "1",
            nullable = true
    )
    private Integer perUserLimit;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Date and time when the coupon becomes active. Must be in the future",
            example = "2025-06-01T00:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Date and time when the coupon expires. Must be after start date",
            example = "2025-08-31T23:59:59",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime endDate;

    @Schema(
            description = "Flag to activate/deactivate the coupon immediately upon creation",
            example = "true",
            nullable = true
    )
    private Boolean isActive;

    @Schema(
            description = "Set of category IDs this coupon applies to. Leave empty to apply to all categories",
            example = "[1, 2, 5]",
            nullable = true
    )
    private Set<Long> applicableCategories;

    @Schema(
            description = "Set of product IDs this coupon applies to. Leave empty to apply to all products",
            example = "[101, 102, 150]",
            nullable = true
    )
    private Set<Long> applicableProducts;
}