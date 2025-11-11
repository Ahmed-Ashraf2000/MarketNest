package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CreateCouponRequest;
import com.marketnest.ecommerce.dto.coupon.UpdateCouponRequest;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.service.coupon.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Coupon Management", description = "APIs for managing coupons")
public class CouponAdminController {

    private final CouponService couponService;

    @Operation(summary = "Get all coupons",
            description = "Retrieves a paginated list of all coupons.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupons retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<CouponResponse> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(coupons);
    }

    @Operation(summary = "Get coupon by ID", description = "Retrieves a coupon by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long couponId) {
        CouponResponse coupon = couponService.getCouponById(couponId);
        return ResponseEntity.ok(coupon);
    }

    @Operation(summary = "Create a new coupon", description = "Creates a new coupon.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coupon created successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Update a coupon", description = "Updates an existing coupon by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon updated successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request) {

        CouponResponse coupon = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(coupon);
    }

    @Operation(summary = "Delete a coupon", description = "Deletes a coupon by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Coupon deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update coupon status",
            description = "Updates the status of a coupon (active/inactive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon status updated successfully",
                    content = @Content(schema = @Schema(implementation = CouponResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    @PatchMapping("/{couponId}/status")
    public ResponseEntity<CouponResponse> updateCouponStatus(
            @PathVariable Long couponId,
            @RequestParam boolean isActive) {

        CouponResponse coupon = couponService.updateCouponStatus(couponId, isActive);
        return ResponseEntity.ok(coupon);
    }
}