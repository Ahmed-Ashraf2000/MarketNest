package com.marketnest.ecommerce.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for coupon validation results. Returns whether coupon is valid and calculated discount")
public class CouponValidationResponse {

    @Schema(description = "Flag indicating if the coupon is valid and can be applied",
            example = "true")
    private boolean valid;

    @Schema(description = "Message explaining validation result (success or reason for failure)",
            example = "Coupon applied successfully")
    private String message;

    @Schema(description = "Calculated discount amount to be subtracted from cart/order total",
            example = "50.00", nullable = true)
    private BigDecimal discountAmount;

    @Schema(description = "Final amount after applying the discount", example = "200.00",
            nullable = true)
    private BigDecimal finalAmount;
}