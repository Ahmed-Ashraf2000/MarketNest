package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.*;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouponUsageRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Coupon testCoupon;
    private User testUser;
    private Order testOrder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        couponUsageRepository.deleteAll();
        orderRepository.deleteAll();
        couponRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser = userRepository.save(testUser);

        testCoupon = new Coupon();
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
        testCoupon = couponRepository.save(testCoupon);

        testOrder = new Order();
        testOrder.setUser(testUser);
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setTotal(new BigDecimal("100.00"));
        testOrder.setShippingAddress(new Address());
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void save_shouldPersistCouponUsage() {
        CouponUsage usage = createCouponUsage(new BigDecimal("10.00"));

        CouponUsage saved = couponUsageRepository.save(usage);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCoupon().getId()).isEqualTo(testCoupon.getId());
        assertThat(saved.getUser().getUserId()).isEqualTo(testUser.getUserId());
        assertThat(saved.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(saved.getUsedAt()).isNotNull();
    }

    @Test
    void countByCoupon_IdAndUser_UserId_shouldReturnCount() {
        couponUsageRepository.save(createCouponUsage(new BigDecimal("10.00")));
        couponUsageRepository.save(createCouponUsage(new BigDecimal("15.00")));

        int count = couponUsageRepository.countByCoupon_IdAndUser_UserId(
                testCoupon.getId(),
                testUser.getUserId()
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByCoupon_IdAndUser_UserId_shouldReturnZero_whenNoUsage() {
        int count = couponUsageRepository.countByCoupon_IdAndUser_UserId(
                testCoupon.getId(),
                999L
        );

        assertThat(count).isZero();
    }

    @Test
    void findByUser_UserId_shouldReturnAllUserUsages() {
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setFirstName("Jane");
        anotherUser.setLastName("Smith");
        anotherUser = userRepository.save(anotherUser);

        couponUsageRepository.save(createCouponUsage(new BigDecimal("10.00")));
        couponUsageRepository.save(createCouponUsage(new BigDecimal("15.00")));

        CouponUsage otherUsage = new CouponUsage();
        otherUsage.setCoupon(testCoupon);
        otherUsage.setUser(anotherUser);
        otherUsage.setDiscountAmount(new BigDecimal("20.00"));
        couponUsageRepository.save(otherUsage);

        List<CouponUsage> userUsages = couponUsageRepository.findByUser_UserId(
                testUser.getUserId()
        );

        assertThat(userUsages).hasSize(2);
        assertThat(userUsages).allMatch(u -> u.getUser().getUserId().equals(testUser.getUserId()));
    }

    @Test
    void findByCoupon_Id_shouldReturnAllCouponUsages() {
        Coupon anotherCoupon = new Coupon();
        anotherCoupon.setCode("ANOTHER10");
        anotherCoupon.setDescription("Another coupon");
        anotherCoupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        anotherCoupon.setDiscountValue(new BigDecimal("20"));
        anotherCoupon.setMinPurchaseAmount(BigDecimal.ZERO);
        anotherCoupon.setUsageCount(0);
        anotherCoupon.setStartDate(LocalDateTime.now().minusDays(1));
        anotherCoupon.setEndDate(LocalDateTime.now().plusDays(30));
        anotherCoupon.setIsActive(true);
        anotherCoupon = couponRepository.save(anotherCoupon);

        couponUsageRepository.save(createCouponUsage(new BigDecimal("10.00")));
        couponUsageRepository.save(createCouponUsage(new BigDecimal("15.00")));

        CouponUsage otherUsage = new CouponUsage();
        otherUsage.setCoupon(anotherCoupon);
        otherUsage.setUser(testUser);
        otherUsage.setDiscountAmount(new BigDecimal("20.00"));
        couponUsageRepository.save(otherUsage);

        List<CouponUsage> couponUsages = couponUsageRepository.findByCoupon_Id(
                testCoupon.getId()
        );

        assertThat(couponUsages).hasSize(2);
        assertThat(couponUsages).allMatch(u -> u.getCoupon().getId().equals(testCoupon.getId()));
    }

    @Test
    void existsByCoupon_IdAndOrder_Id_shouldReturnTrue_whenUsageExists() {
        CouponUsage usage = createCouponUsage(new BigDecimal("10.00"));
        usage.setOrder(testOrder);
        couponUsageRepository.save(usage);

        boolean exists = couponUsageRepository.existsByCoupon_IdAndOrder_Id(
                testCoupon.getId(),
                testOrder.getId()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCoupon_IdAndOrder_Id_shouldReturnFalse_whenUsageDoesNotExist() {
        boolean exists = couponUsageRepository.existsByCoupon_IdAndOrder_Id(
                testCoupon.getId(),
                999L
        );

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldHandleNullOrder() {
        CouponUsage usage = createCouponUsage(new BigDecimal("10.00"));
        usage.setOrder(null);

        CouponUsage saved = couponUsageRepository.save(usage);

        assertThat(saved.getOrder()).isNull();
    }

    @Test
    void delete_shouldRemoveCouponUsage() {
        CouponUsage usage = createCouponUsage(new BigDecimal("10.00"));
        CouponUsage saved = couponUsageRepository.save(usage);

        couponUsageRepository.deleteById(saved.getId());

        List<CouponUsage> remaining = couponUsageRepository.findAll();
        assertThat(remaining).isEmpty();
    }

    @Test
    void save_shouldHandleDifferentDiscountAmounts() {
        CouponUsage usage1 = createCouponUsage(new BigDecimal("5.50"));
        CouponUsage usage2 = createCouponUsage(new BigDecimal("25.99"));
        CouponUsage usage3 = createCouponUsage(new BigDecimal("100.00"));

        couponUsageRepository.save(usage1);
        couponUsageRepository.save(usage2);
        couponUsageRepository.save(usage3);

        List<CouponUsage> allUsages = couponUsageRepository.findAll();

        assertThat(allUsages).hasSize(3);
        assertThat(allUsages).extracting(CouponUsage::getDiscountAmount)
                .containsExactlyInAnyOrder(
                        new BigDecimal("5.50"),
                        new BigDecimal("25.99"),
                        new BigDecimal("100.00")
                );
    }

    private CouponUsage createCouponUsage(BigDecimal discountAmount) {
        CouponUsage usage = new CouponUsage();
        usage.setCoupon(testCoupon);
        usage.setUser(testUser);
        usage.setDiscountAmount(discountAmount);
        return usage;
    }
}