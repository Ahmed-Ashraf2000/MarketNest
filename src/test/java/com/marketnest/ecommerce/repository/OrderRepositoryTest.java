package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testUser;
    private Address testAddress;
    private Product testProduct;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser = userRepository.save(testUser);

        testAddress = new Address();
        testAddress.setUser(testUser);
        testAddress.setAddressLine1("123 Main St");
        testAddress.setCity("Test City");
        testAddress.setStateProvince("TS");
        testAddress.setCountryCode("EGY");
        testAddress.setPostalCode("12345");
        testAddress = addressRepository.save(testAddress);

        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setSlug("test-product");
        testProduct.setSku("TEST-001");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory(category);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void save_shouldPersistOrder() {
        Order order = createOrder(Order.OrderStatus.PENDING);

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getUserId()).isEqualTo(testUser.getUserId());
        assertThat(saved.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUser_UserId_shouldReturnUserOrders() {
        Order order1 = createOrder(Order.OrderStatus.PENDING);
        Order order2 = createOrder(Order.OrderStatus.PROCESSING);

        orderRepository.save(order1);
        orderRepository.save(order2);

        Page<Order> orders = orderRepository.findByUser_UserId(
                testUser.getUserId(),
                PageRequest.of(0, 10)
        );

        assertThat(orders.getContent()).hasSize(2);
        assertThat(orders.getContent()).allMatch(
                o -> o.getUser().getUserId().equals(testUser.getUserId()));
    }

    @Test
    void findByUser_UserIdAndId_shouldReturnOrder_whenExists() {
        Order order = createOrder(Order.OrderStatus.PENDING);
        Order saved = orderRepository.save(order);

        Optional<Order> found = orderRepository.findByUser_UserIdAndId(
                saved.getId(),
                testUser.getUserId()
        );

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findByUser_UserIdAndId_shouldReturnEmpty_whenDifferentUser() {
        Order order = createOrder(Order.OrderStatus.PENDING);
        Order saved = orderRepository.save(order);

        Optional<Order> found = orderRepository.findByUser_UserIdAndId(saved.getId(), 999L);

        assertThat(found).isEmpty();
    }

    @Test
    void existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn_shouldReturnTrue_whenExists() {
        Order order = createOrder(Order.OrderStatus.DELIVERED);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(testProduct.getPrice());
        orderItem.setTotalPrice(testProduct.getPrice());
        order.addOrderItem(orderItem);

        orderRepository.save(order);

        boolean exists = orderRepository.existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn(
                testUser.getUserId(),
                testProduct.getId(),
                Arrays.asList(Order.OrderStatus.DELIVERED, Order.OrderStatus.SHIPPED)
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn_shouldReturnFalse_whenStatusNotMatched() {
        Order order = createOrder(Order.OrderStatus.PENDING);
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(testProduct.getPrice());
        orderItem.setTotalPrice(testProduct.getPrice());
        order.addOrderItem(orderItem);

        orderRepository.save(order);

        boolean exists = orderRepository.existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn(
                testUser.getUserId(),
                testProduct.getId(),
                Arrays.asList(Order.OrderStatus.DELIVERED, Order.OrderStatus.SHIPPED)
        );

        assertThat(exists).isFalse();
    }

    @Test
    void calculateTotalRevenue_shouldSumAllNonCancelledOrders() {
        Order order1 = createOrder(Order.OrderStatus.DELIVERED);
        order1.setTotal(new BigDecimal("100.00"));
        Order order2 = createOrder(Order.OrderStatus.PROCESSING);
        order2.setTotal(new BigDecimal("200.00"));
        Order order3 = createOrder(Order.OrderStatus.CANCELLED);
        order3.setTotal(new BigDecimal("50.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);

        BigDecimal revenue = orderRepository.calculateTotalRevenue();

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void calculateAverageOrderValue_shouldCalculateCorrectly() {
        Order order1 = createOrder(Order.OrderStatus.DELIVERED);
        order1.setTotal(new BigDecimal("100.00"));
        Order order2 = createOrder(Order.OrderStatus.PROCESSING);
        order2.setTotal(new BigDecimal("200.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);

        BigDecimal average = orderRepository.calculateAverageOrderValue();

        assertThat(average).isNotNull();
        assertThat(average.compareTo(new BigDecimal("150.00"))).isEqualTo(0);
    }

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        createAndSaveOrder(Order.OrderStatus.PENDING);
        createAndSaveOrder(Order.OrderStatus.PENDING);
        createAndSaveOrder(Order.OrderStatus.PROCESSING);

        Long count = orderRepository.countByStatus(Order.OrderStatus.PENDING);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void calculateRevenueForPeriod_shouldFilterByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        Order order1 = createOrder(Order.OrderStatus.DELIVERED);
        order1.setOrderDate(now.minusDays(5));
        order1.setTotal(new BigDecimal("100.00"));

        Order order2 = createOrder(Order.OrderStatus.DELIVERED);
        order2.setOrderDate(now.minusDays(15));
        order2.setTotal(new BigDecimal("200.00"));

        orderRepository.save(order1);
        orderRepository.save(order2);

        BigDecimal revenue = orderRepository.calculateRevenueForPeriod(
                now.minusDays(10),
                now
        );

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void countOrdersForPeriod_shouldFilterByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        Order order1 = createOrder(Order.OrderStatus.DELIVERED);
        order1.setOrderDate(now.minusDays(5));

        Order order2 = createOrder(Order.OrderStatus.DELIVERED);
        order2.setOrderDate(now.minusDays(15));

        orderRepository.save(order1);
        orderRepository.save(order2);

        Long count = orderRepository.countOrdersForPeriod(now.minusDays(10), now);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countDistinctCustomers_shouldReturnUniqueUsers() {
        createAndSaveOrder(Order.OrderStatus.DELIVERED);
        createAndSaveOrder(Order.OrderStatus.PROCESSING);

        Long customerCount = orderRepository.countDistinctCustomers();

        assertThat(customerCount).isEqualTo(1);
    }

    @Test
    void findOrderCountByStatus_shouldGroupByStatus() {
        createAndSaveOrder(Order.OrderStatus.PENDING);
        createAndSaveOrder(Order.OrderStatus.PENDING);
        createAndSaveOrder(Order.OrderStatus.PROCESSING);

        List<Object[]> statusCounts = orderRepository.findOrderCountByStatus();

        assertThat(statusCounts).isNotEmpty();
    }

    private Order createOrder(Order.OrderStatus status) {
        Order order = new Order();
        order.setUser(testUser);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(status);
        order.setShippingAddress(testAddress);
        order.setBillingAddress(testAddress);
        order.setSubtotal(new BigDecimal("99.99"));
        order.setShippingCost(new BigDecimal("10.00"));
        order.setTax(new BigDecimal("5.00"));
        order.setDiscount(BigDecimal.ZERO);
        order.setTotal(new BigDecimal("114.99"));
        return order;
    }

    private void createAndSaveOrder(Order.OrderStatus status) {
        orderRepository.save(createOrder(status));
    }
}