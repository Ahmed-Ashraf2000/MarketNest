package com.marketnest.ecommerce.dto.coupon;

import com.marketnest.ecommerce.model.Coupon;
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
public class CouponResponse {
    private Long id;
    private String code;
    private String description;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer perUserLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Set<Long> applicableCategories;
    private Set<Long> applicableProducts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}