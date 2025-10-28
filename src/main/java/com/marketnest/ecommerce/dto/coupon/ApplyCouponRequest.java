package com.marketnest.ecommerce.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;
}