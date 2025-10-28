package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.coupon.ApplyCouponRequest;
import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CouponValidationResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.coupon.CouponService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserRepository userRepository;

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            @RequestParam @DecimalMin(value = "0.01") BigDecimal orderAmount,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        CouponValidationResponse response = couponService.validateCoupon(
                request.getCode(), userId, orderAmount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<CouponResponse>> getAvailableCoupons(
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        List<CouponResponse> coupons = couponService.getAvailableCoupons(userId);

        return ResponseEntity.ok(coupons);
    }

    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("The user not found"));

        return user.getUserId();
    }
}