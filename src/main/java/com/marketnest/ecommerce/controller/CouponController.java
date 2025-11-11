package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.coupon.ApplyCouponRequest;
import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CouponValidationResponse;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Coupon Management", description = "APIs for managing and validating coupons")
public class CouponController {

    private final CouponService couponService;
    private final UserRepository userRepository;

    @Operation(summary = "Validate a coupon",
            description = "Validates a coupon for the authenticated user and order amount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon validated successfully",
                    content = @Content(
                            schema = @Schema(implementation = CouponValidationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
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

    @Operation(summary = "Get available coupons",
            description = "Retrieves all available coupons for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Available coupons retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    })
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