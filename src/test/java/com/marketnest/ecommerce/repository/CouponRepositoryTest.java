package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouponRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CouponRepository couponRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void save_shouldPersistCoupon() {
        Coupon coupon = createCoupon("SAVE10", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);

        Coupon saved = couponRepository.save(coupon);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("SAVE10");
        assertThat(saved.getDiscountType()).isEqualTo(Coupon.DiscountType.PERCENTAGE);
        assertThat(saved.getDiscountValue()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getUsageCount()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByCodeAndIsActiveTrue_shouldReturnCoupon_whenExists() {
        Coupon coupon = createCoupon("ACTIVE10", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        couponRepository.save(coupon);

        Optional<Coupon> found = couponRepository.findByCodeAndIsActiveTrue("ACTIVE10");

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("ACTIVE10");
        assertThat(found.get().getIsActive()).isTrue();
    }

    @Test
    void findByCodeAndIsActiveTrue_shouldReturnEmpty_whenInactive() {
        Coupon coupon = createCoupon("INACTIVE10", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), false);
        couponRepository.save(coupon);

        Optional<Coupon> found = couponRepository.findByCodeAndIsActiveTrue("INACTIVE10");

        assertThat(found).isEmpty();
    }

    @Test
    void findByCode_shouldReturnCoupon_regardlessOfStatus() {
        Coupon activeCoupon = createCoupon("ACTIVE", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        Coupon inactiveCoupon = createCoupon("INACTIVE", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("20"), false);

        couponRepository.save(activeCoupon);
        couponRepository.save(inactiveCoupon);

        Optional<Coupon> foundActive = couponRepository.findByCode("ACTIVE");
        Optional<Coupon> foundInactive = couponRepository.findByCode("INACTIVE");

        assertThat(foundActive).isPresent();
        assertThat(foundInactive).isPresent();
    }

    @Test
    void existsByCode_shouldReturnTrue_whenCouponExists() {
        Coupon coupon = createCoupon("EXISTS", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        couponRepository.save(coupon);

        boolean exists = couponRepository.existsByCode("EXISTS");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_shouldReturnFalse_whenCouponDoesNotExist() {
        boolean exists = couponRepository.existsByCode("NOTEXISTS");

        assertThat(exists).isFalse();
    }

    @Test
    void findAvailableCoupons_shouldReturnOnlyValidCoupons() {
        LocalDateTime now = LocalDateTime.now();

        Coupon validCoupon = createCoupon("VALID", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        validCoupon.setStartDate(now.minusDays(1));
        validCoupon.setEndDate(now.plusDays(1));

        Coupon expiredCoupon = createCoupon("EXPIRED", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("20"), true);
        expiredCoupon.setStartDate(now.minusDays(10));
        expiredCoupon.setEndDate(now.minusDays(1));

        Coupon futureCoupon = createCoupon("FUTURE", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("15"), true);
        futureCoupon.setStartDate(now.plusDays(1));
        futureCoupon.setEndDate(now.plusDays(10));

        couponRepository.save(validCoupon);
        couponRepository.save(expiredCoupon);
        couponRepository.save(futureCoupon);

        List<Coupon> availableCoupons = couponRepository.findAvailableCoupons(now);

        assertThat(availableCoupons).hasSize(1);
        assertThat(availableCoupons.getFirst().getCode()).isEqualTo("VALID");
    }

    @Test
    void findAvailableCoupons_shouldExcludeFullyUsedCoupons() {
        LocalDateTime now = LocalDateTime.now();

        Coupon fullyUsedCoupon = createCoupon("FULLUSED", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        fullyUsedCoupon.setStartDate(now.minusDays(1));
        fullyUsedCoupon.setEndDate(now.plusDays(1));
        fullyUsedCoupon.setUsageLimit(5);
        fullyUsedCoupon.setUsageCount(5);

        Coupon availableCoupon = createCoupon("AVAILABLE", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("20"), true);
        availableCoupon.setStartDate(now.minusDays(1));
        availableCoupon.setEndDate(now.plusDays(1));
        availableCoupon.setUsageLimit(10);
        availableCoupon.setUsageCount(5);

        couponRepository.save(fullyUsedCoupon);
        couponRepository.save(availableCoupon);

        List<Coupon> availableCoupons = couponRepository.findAvailableCoupons(now);

        assertThat(availableCoupons).hasSize(1);
        assertThat(availableCoupons.getFirst().getCode()).isEqualTo("AVAILABLE");
    }

    @Test
    void findApplicableCoupons_shouldReturnCouponsForCategory() {
        LocalDateTime now = LocalDateTime.now();

        Coupon categoryCoupon = createCoupon("CATEGORY10", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        categoryCoupon.setStartDate(now.minusDays(1));
        categoryCoupon.setEndDate(now.plusDays(1));
        Set<Long> categories = new HashSet<>();
        categories.add(1L);
        categoryCoupon.setApplicableCategories(categories);

        Coupon genericCoupon = createCoupon("GENERIC10", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("15"), true);
        genericCoupon.setStartDate(now.minusDays(1));
        genericCoupon.setEndDate(now.plusDays(1));

        couponRepository.save(categoryCoupon);
        couponRepository.save(genericCoupon);

        List<Coupon> applicableCoupons = couponRepository.findApplicableCoupons(1L, 100L, now);

        assertThat(applicableCoupons).hasSize(2);
    }

    @Test
    void findApplicableCoupons_shouldReturnCouponsForProduct() {
        LocalDateTime now = LocalDateTime.now();

        Coupon productCoupon = createCoupon("PRODUCT10", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        productCoupon.setStartDate(now.minusDays(1));
        productCoupon.setEndDate(now.plusDays(1));
        Set<Long> products = new HashSet<>();
        products.add(100L);
        productCoupon.setApplicableProducts(products);

        couponRepository.save(productCoupon);

        List<Coupon> applicableCoupons = couponRepository.findApplicableCoupons(1L, 100L, now);

        assertThat(applicableCoupons).hasSize(1);
        assertThat(applicableCoupons.getFirst().getCode()).isEqualTo("PRODUCT10");
    }

    @Test
    void save_shouldHandleFixedAmountDiscount() {
        Coupon coupon = createCoupon("FIXED50", Coupon.DiscountType.FIXED_AMOUNT,
                new BigDecimal("50.00"), true);

        Coupon saved = couponRepository.save(coupon);

        assertThat(saved.getDiscountType()).isEqualTo(Coupon.DiscountType.FIXED_AMOUNT);
        assertThat(saved.getDiscountValue()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void save_shouldHandleMinPurchaseAmount() {
        Coupon coupon = createCoupon("MIN100", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        coupon.setMinPurchaseAmount(new BigDecimal("100.00"));

        Coupon saved = couponRepository.save(coupon);

        assertThat(saved.getMinPurchaseAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void save_shouldHandleMaxDiscountAmount() {
        Coupon coupon = createCoupon("MAX50", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("20"), true);
        coupon.setMaxDiscountAmount(new BigDecimal("50.00"));

        Coupon saved = couponRepository.save(coupon);

        assertThat(saved.getMaxDiscountAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void delete_shouldRemoveCoupon() {
        Coupon coupon = createCoupon("DELETE", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        Coupon saved = couponRepository.save(coupon);

        couponRepository.deleteById(saved.getId());

        Optional<Coupon> found = couponRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void isValid_shouldReturnTrue_whenCouponIsValid() {
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = createCoupon("VALID", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        coupon.setStartDate(now.minusDays(1));
        coupon.setEndDate(now.plusDays(1));
        coupon.setUsageLimit(10);
        coupon.setUsageCount(5);

        Coupon saved = couponRepository.save(coupon);

        assertThat(saved.isValid()).isTrue();
    }

    @Test
    void incrementUsageCount_shouldIncrementCount() {
        Coupon coupon = createCoupon("COUNT", Coupon.DiscountType.PERCENTAGE,
                new BigDecimal("10"), true);
        Coupon saved = couponRepository.save(coupon);

        saved.incrementUsageCount();
        saved.incrementUsageCount();
        Coupon updated = couponRepository.save(saved);

        assertThat(updated.getUsageCount()).isEqualTo(2);
    }

    private Coupon createCoupon(String code, Coupon.DiscountType discountType,
                                BigDecimal discountValue, boolean isActive) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDescription("Test coupon");
        coupon.setDiscountType(discountType);
        coupon.setDiscountValue(discountValue);
        coupon.setMinPurchaseAmount(BigDecimal.ZERO);
        coupon.setUsageCount(0);
        coupon.setPerUserLimit(1);
        coupon.setStartDate(LocalDateTime.now().minusDays(1));
        coupon.setEndDate(LocalDateTime.now().plusDays(30));
        coupon.setIsActive(isActive);
        return coupon;
    }
}