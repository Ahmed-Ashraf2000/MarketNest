package com.marketnest.ecommerce.service.coupon;

import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CouponValidationResponse;
import com.marketnest.ecommerce.dto.coupon.CreateCouponRequest;
import com.marketnest.ecommerce.dto.coupon.UpdateCouponRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    CouponValidationResponse validateCoupon(String code, Long userId, BigDecimal orderAmount);

    List<CouponResponse> getAvailableCoupons(Long userId);

    Page<CouponResponse> getAllCoupons(Pageable pageable);

    CouponResponse getCouponById(Long couponId);

    CouponResponse createCoupon(CreateCouponRequest request);

    CouponResponse updateCoupon(Long couponId, UpdateCouponRequest request);

    void deleteCoupon(Long couponId);

    CouponResponse updateCouponStatus(Long couponId, boolean isActive);

    void applyCoupon(Long couponId, Long userId, Long orderId, BigDecimal discountAmount);
}