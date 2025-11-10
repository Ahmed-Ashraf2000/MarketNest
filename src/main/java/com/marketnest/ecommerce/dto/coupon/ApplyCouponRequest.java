package com.marketnest.ecommerce.dto.coupon;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for applying a coupon code to a cart or order")
public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Schema(
            description = "Coupon code to apply. Must match an active coupon exactly (case-sensitive)",
            example = "SUMMER2025",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;
}