package com.marketnest.ecommerce.dto.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Data Transfer Object for updating an existing coupon (admin operation). All fields are optional")
public class UpdateCouponRequest {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(
            description = "Updated description for the coupon",
            example = "Extended summer sale - 20% off on all electronics through September",
            maxLength = 500,
            nullable = true
    )
    private String description;

    @Schema(
            description = "Updated discount type",
            example = "FIXED_AMOUNT",
            allowableValues = {"PERCENTAGE", "FIXED_AMOUNT"},
            nullable = true
    )
    private Coupon.DiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @Schema(
            description = "Updated discount value - percentage or fixed amount based on discount type",
            example = "25.00",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Minimum purchase amount must be non-negative")
    @Schema(
            description = "Updated minimum purchase requirement",
            example = "75.00",
            minimum = "0.00",
            nullable = true
    )
    private BigDecimal minPurchaseAmount;

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    @Schema(
            description = "Updated maximum discount cap",
            example = "150.00",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    @Schema(
            description = "Updated total usage limit across all users",
            example = "2000",
            minimum = "1",
            nullable = true
    )
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    @Schema(
            description = "Updated per-user usage limit",
            example = "2",
            minimum = "1",
            nullable = true
    )
    private Integer perUserLimit;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Updated expiration date for the coupon",
            example = "2025-09-30T23:59:59",
            nullable = true
    )
    private LocalDateTime endDate;

    @Schema(
            description = "Updated active status to enable/disable the coupon",
            example = "false",
            nullable = true
    )
    private Boolean isActive;

    @Schema(
            description = "Updated set of applicable category IDs",
            example = "[1, 2, 5, 8]",
            nullable = true
    )
    private Set<Long> applicableCategories;

    @Schema(
            description = "Updated set of applicable product IDs",
            example = "[101, 102, 150, 200]",
            nullable = true
    )
    private Set<Long> applicableProducts;
}