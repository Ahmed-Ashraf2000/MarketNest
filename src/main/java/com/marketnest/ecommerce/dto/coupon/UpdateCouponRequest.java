package com.marketnest.ecommerce.dto.coupon;

import com.marketnest.ecommerce.model.Coupon;
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
public class UpdateCouponRequest {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Coupon.DiscountType discountType;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Minimum purchase amount must be non-negative")
    private BigDecimal minPurchaseAmount;

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit;

    private LocalDateTime endDate;

    private Boolean isActive;

    private Set<Long> applicableCategories;

    private Set<Long> applicableProducts;
}