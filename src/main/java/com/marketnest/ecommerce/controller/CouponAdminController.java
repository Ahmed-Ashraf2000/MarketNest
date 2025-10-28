package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CreateCouponRequest;
import com.marketnest.ecommerce.dto.coupon.UpdateCouponRequest;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.service.coupon.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class CouponAdminController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<CouponResponse> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long couponId) {
        CouponResponse coupon = couponService.getCouponById(couponId);
        return ResponseEntity.ok(coupon);
    }

    @PostMapping
    public ResponseEntity<?> createCoupon(
            @Valid @RequestBody CreateCouponRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        CouponResponse coupon = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request) {

        CouponResponse coupon = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(coupon);
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{couponId}/status")
    public ResponseEntity<CouponResponse> updateCouponStatus(
            @PathVariable Long couponId,
            @RequestParam boolean isActive) {

        CouponResponse coupon = couponService.updateCouponStatus(couponId, isActive);
        return ResponseEntity.ok(coupon);
    }
}