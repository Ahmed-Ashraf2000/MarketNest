package com.marketnest.ecommerce.service.coupon;

import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CouponValidationResponse;
import com.marketnest.ecommerce.dto.coupon.CreateCouponRequest;
import com.marketnest.ecommerce.dto.coupon.UpdateCouponRequest;
import com.marketnest.ecommerce.exception.*;
import com.marketnest.ecommerce.mapper.coupon.CouponMapper;
import com.marketnest.ecommerce.model.Coupon;
import com.marketnest.ecommerce.model.CouponUsage;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.CouponRepository;
import com.marketnest.ecommerce.repository.CouponUsageRepository;
import com.marketnest.ecommerce.repository.OrderRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, Long userId,
                                                   BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new InvalidCouponException("Invalid or inactive coupon code"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon is not active yet")
                    .build();
        }

        if (now.isAfter(coupon.getEndDate())) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon has expired")
                    .build();
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("Coupon usage limit reached")
                    .build();
        }

        int userUsageCount = couponUsageRepository.countByCoupon_IdAndUser_UserId(
                coupon.getId(), userId);
        if (coupon.getPerUserLimit() != null && userUsageCount >= coupon.getPerUserLimit()) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message("You have already used this coupon maximum times")
                    .build();
        }

        if (coupon.getMinPurchaseAmount() != null &&
            orderAmount.compareTo(coupon.getMinPurchaseAmount()) < 0) {
            return CouponValidationResponse.builder()
                    .valid(false)
                    .message(String.format("Minimum purchase amount of $%.2f required",
                            coupon.getMinPurchaseAmount()))
                    .build();
        }

        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        return CouponValidationResponse.builder()
                .valid(true)
                .message("Coupon applied successfully")
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> availableCoupons = couponRepository.findAvailableCoupons(now);

        return availableCoupons.stream()
                .filter(coupon -> {
                    int userUsageCount = couponUsageRepository.countByCoupon_IdAndUser_UserId(
                            coupon.getId(), userId);
                    return coupon.getPerUserLimit() == null ||
                           userUsageCount < coupon.getPerUserLimit();
                })
                .map(couponMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(couponMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with id: " + couponId));
        return couponMapper.toResponse(coupon);
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException(
                    "Coupon with code " + request.getCode() + " already exists");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidCouponException("End date must be after start date");
        }

        if (request.getDiscountType() == Coupon.DiscountType.PERCENTAGE &&
            request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidCouponException(
                    "Percentage discount cannot exceed 100%");
        }

        Coupon coupon = couponMapper.toEntity(request);
        Coupon savedCoupon = couponRepository.save(coupon);
        return couponMapper.toResponse(savedCoupon);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long couponId, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with id: " + couponId));

        if (request.getEndDate() != null && request.getEndDate().isBefore(coupon.getStartDate())) {
            throw new InvalidCouponException("End date must be after start date");
        }

        if (request.getDiscountType() == Coupon.DiscountType.PERCENTAGE &&
            request.getDiscountValue() != null &&
            request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidCouponException(
                    "Percentage discount cannot exceed 100%");
        }

        couponMapper.updateEntityFromDto(request, coupon);
        Coupon updatedCoupon = couponRepository.save(coupon);
        return couponMapper.toResponse(updatedCoupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with id: " + couponId));

        couponRepository.delete(coupon);
    }

    @Override
    @Transactional
    public CouponResponse updateCouponStatus(Long couponId, boolean isActive) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with id: " + couponId));

        coupon.setIsActive(isActive);
        Coupon updatedCoupon = couponRepository.save(coupon);
        return couponMapper.toResponse(updatedCoupon);
    }

    @Override
    @Transactional
    public void applyCoupon(Long couponId, Long userId, Long orderId, BigDecimal discountAmount) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(
                        "Coupon not found with id: " + couponId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with id: " + orderId));

        if (couponUsageRepository.existsByCoupon_IdAndOrder_Id(couponId, orderId)) {
            throw new InvalidCouponException("Coupon already applied to this order");
        }

        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setUser(user);
        usage.setOrder(order);
        usage.setDiscountAmount(discountAmount);

        couponUsageRepository.save(usage);
        coupon.incrementUsageCount();
        couponRepository.save(coupon);
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;

        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        // Apply max discount limit if specified
        if (coupon.getMaxDiscountAmount() != null &&
            discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        // Ensure discount doesn't exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}