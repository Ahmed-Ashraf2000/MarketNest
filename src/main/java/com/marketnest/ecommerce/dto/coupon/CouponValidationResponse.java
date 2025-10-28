package com.marketnest.ecommerce.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponValidationResponse {
    private boolean valid;
    private String message;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}