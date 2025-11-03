package com.marketnest.ecommerce.service.coupon;

import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CouponValidationResponse;
import com.marketnest.ecommerce.dto.coupon.CreateCouponRequest;
import com.marketnest.ecommerce.dto.coupon.UpdateCouponRequest;
import com.marketnest.ecommerce.exception.CouponNotFoundException;
import com.marketnest.ecommerce.mapper.coupon.CouponMapper;
import com.marketnest.ecommerce.model.Coupon;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.CouponRepository;
import com.marketnest.ecommerce.repository.CouponUsageRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon testCoupon;
    private User testUser;
    private CreateCouponRequest createRequest;
    private UpdateCouponRequest updateRequest;
    private CouponResponse couponResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        testCoupon = new Coupon();
        testCoupon.setId(1L);
        testCoupon.setCode("TEST10");
        testCoupon.setDescription("Test coupon");
        testCoupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        testCoupon.setDiscountValue(new BigDecimal("10"));
        testCoupon.setMinPurchaseAmount(BigDecimal.ZERO);
        testCoupon.setUsageCount(0);
        testCoupon.setPerUserLimit(3);
        testCoupon.setStartDate(LocalDateTime.now().minusDays(1));
        testCoupon.setEndDate(LocalDateTime.now().plusDays(30));
        testCoupon.setIsActive(true);

        createRequest = new CreateCouponRequest();
        createRequest.setCode("NEW10");
        createRequest.setDescription("New coupon");
        createRequest.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        createRequest.setDiscountValue(new BigDecimal("10"));
        createRequest.setStartDate(LocalDateTime.now());
        createRequest.setEndDate(LocalDateTime.now().plusDays(30));

        updateRequest = new UpdateCouponRequest();
        updateRequest.setDescription("Updated coupon");
        updateRequest.setDiscountValue(new BigDecimal("15"));

        couponResponse = new CouponResponse();
        couponResponse.setId(1L);
        couponResponse.setCode("TEST10");
    }

    @Test
    void getAllCoupons_shouldReturnPagedCoupons() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Coupon> couponPage = new PageImpl<>(Collections.singletonList(testCoupon));

        when(couponRepository.findAll(pageable)).thenReturn(couponPage);
        when(couponMapper.toResponse(testCoupon)).thenReturn(couponResponse);

        Page<CouponResponse> result = couponService.getAllCoupons(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(couponRepository).findAll(pageable);
    }

    @Test
    void getCouponById_shouldReturnCoupon_whenExists() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponMapper.toResponse(testCoupon)).thenReturn(couponResponse);

        CouponResponse result = couponService.getCouponById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(couponRepository).findById(1L);
    }

    @Test
    void getCouponById_shouldThrowException_whenNotFound() {
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getCouponById(999L))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void createCoupon_shouldCreateCoupon_whenValid() {
        when(couponRepository.existsByCode("NEW10")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);
        when(couponMapper.toResponse(testCoupon)).thenReturn(couponResponse);

        CouponResponse result = couponService.createCoupon(createRequest);

        assertThat(result).isNotNull();
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void createCoupon_shouldThrowException_whenCodeExists() {
        when(couponRepository.existsByCode("NEW10")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(createRequest))
                .isInstanceOf(BadRequestException.class);

        verify(couponRepository, never()).save(any());
    }

    @Test
    void updateCoupon_shouldUpdateCoupon_whenExists() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);
        when(couponMapper.toResponse(testCoupon)).thenReturn(couponResponse);

        CouponResponse result = couponService.updateCoupon(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void deleteCoupon_shouldDeleteCoupon_whenExists() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        couponService.deleteCoupon(1L);

        verify(couponRepository).delete(testCoupon);
    }

    @Test
    void updateCouponStatus_shouldUpdateStatus() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(testCoupon);
        when(couponMapper.toResponse(testCoupon)).thenReturn(couponResponse);

        CouponResponse result = couponService.updateCouponStatus(1L, false);

        assertThat(result).isNotNull();
        verify(couponRepository).save(argThat(coupon -> !coupon.getIsActive()));
    }

    @Test
    void validateCoupon_shouldReturnValid_whenCouponIsValid() {
        when(couponRepository.findByCodeAndIsActiveTrue("TEST10"))
                .thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(0);

        CouponValidationResponse result = couponService.validateCoupon(
                "TEST10",
                1L,
                new BigDecimal("100.00")
        );

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isNotNull();
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenCouponNotFound() {
        when(couponRepository.findByCodeAndIsActiveTrue("INVALID"))
                .thenReturn(Optional.empty());

        CouponValidationResponse result = couponService.validateCoupon(
                "INVALID",
                1L,
                new BigDecimal("100.00")
        );

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("not found");
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenBelowMinPurchase() {
        testCoupon.setMinPurchaseAmount(new BigDecimal("100.00"));

        when(couponRepository.findByCodeAndIsActiveTrue("TEST10"))
                .thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        CouponValidationResponse result = couponService.validateCoupon(
                "TEST10",
                1L,
                new BigDecimal("50.00")
        );

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("minimum purchase");
    }

    @Test
    void validateCoupon_shouldReturnInvalid_whenUserLimitExceeded() {
        when(couponRepository.findByCodeAndIsActiveTrue("TEST10"))
                .thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(3);

        CouponValidationResponse result = couponService.validateCoupon(
                "TEST10",
                1L,
                new BigDecimal("100.00")
        );

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("limit");
    }

    @Test
    void validateCoupon_shouldCalculatePercentageDiscount() {
        when(couponRepository.findByCodeAndIsActiveTrue("TEST10"))
                .thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(0);

        CouponValidationResponse result = couponService.validateCoupon(
                "TEST10",
                1L,
                new BigDecimal("100.00")
        );

        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void validateCoupon_shouldCalculateFixedAmountDiscount() {
        testCoupon.setDiscountType(Coupon.DiscountType.FIXED_AMOUNT);
        testCoupon.setDiscountValue(new BigDecimal("25.00"));

        when(couponRepository.findByCodeAndIsActiveTrue("TEST10"))
                .thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(0);

        CouponValidationResponse result = couponService.validateCoupon(
                "TEST10",
                1L,
                new BigDecimal("100.00")
        );

        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void validateCoupon_shouldApplyMaxDiscount() {
        testCoupon.setMaxDiscountAmount(new BigDecimal("5.00"));

        when(couponRepository.findByCodeAndIsActiveTrue("TEST10"))
                .thenReturn(Optional.of(testCoupon));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(0);

        CouponValidationResponse result = couponService.validateCoupon(
                "TEST10",
                1L,
                new BigDecimal("100.00")
        );

        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    void getAvailableCoupons_shouldReturnAvailableCoupons() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponRepository.findAvailableCoupons(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testCoupon));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(0);
        when(couponMapper.toResponse(testCoupon)).thenReturn(couponResponse);

        List<CouponResponse> result = couponService.getAvailableCoupons(1L);

        assertThat(result).hasSize(1);
        verify(couponRepository).findAvailableCoupons(any(LocalDateTime.class));
    }

    @Test
    void getAvailableCoupons_shouldExcludeCouponsAtUserLimit() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(couponRepository.findAvailableCoupons(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testCoupon));
        when(couponUsageRepository.countByCoupon_IdAndUser_UserId(1L, 1L)).thenReturn(3);

        List<CouponResponse> result = couponService.getAvailableCoupons(1L);

        assertThat(result).isEmpty();
    }
}