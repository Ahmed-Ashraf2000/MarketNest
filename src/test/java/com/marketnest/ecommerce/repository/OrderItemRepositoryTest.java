package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.dto.analytics.TopSellingProductDto;
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
class OrderItemRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private OrderItemRepository orderItemRepository;

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

    private Order testOrder;
    private Product testProduct1;
    private Product testProduct2;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Ahmed");
        user.setLastName("Ashraf");
        user = userRepository.save(user);

        Address address = new Address();
        address.setUser(user);
        address.setAddressLine1("123 Main St");
        address.setCity("Test City");
        address.setStateProvince("TS");
        address.setCountryCode("EGY");
        address.setPostalCode("12345");
        address = addressRepository.save(address);

        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category = categoryRepository.save(category);

        testProduct1 = new Product();
        testProduct1.setName("Product 1");
        testProduct1.setSlug("product-1");
        testProduct1.setSku("PROD-001");
        testProduct1.setPrice(new BigDecimal("99.99"));
        testProduct1.setStockQuantity(100);
        testProduct1.setCategory(category);
        testProduct1.setIsActive(true);
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Product 2");
        testProduct2.setSlug("product-2");
        testProduct2.setSku("PROD-002");
        testProduct2.setPrice(new BigDecimal("149.99"));
        testProduct2.setStockQuantity(50);
        testProduct2.setCategory(category);
        testProduct2.setIsActive(true);
        testProduct2 = productRepository.save(testProduct2);

        testOrder = new Order();
        testOrder.setUser(user);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        testOrder.setShippingAddress(address);
        testOrder.setBillingAddress(address);
        testOrder.setSubtotal(new BigDecimal("99.99"));
        testOrder.setShippingCost(new BigDecimal("10.00"));
        testOrder.setTax(new BigDecimal("5.00"));
        testOrder.setDiscount(BigDecimal.ZERO);
        testOrder.setTotal(new BigDecimal("114.99"));
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void save_shouldPersistOrderItem() {
        OrderItem orderItem = createOrderItem(testProduct1, 2);

        OrderItem saved = orderItemRepository.save(orderItem);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getUnitPrice()).isEqualByComparingTo(testProduct1.getPrice());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findTopSellingProducts_shouldReturnProductsByQuantity() {
        OrderItem item1 = createOrderItem(testProduct1, 10);
        OrderItem item2 = createOrderItem(testProduct2, 5);

        orderItemRepository.save(item1);
        orderItemRepository.save(item2);

        List<TopSellingProductDto> topProducts = orderItemRepository.findTopSellingProducts(10);

        assertThat(topProducts).isNotEmpty();
        assertThat(topProducts.getFirst().getTotalQuantitySold()).isGreaterThanOrEqualTo(
                topProducts.getLast().getTotalQuantitySold()
        );
    }

    @Test
    void findTopSellingProducts_shouldExcludeCancelledOrders() {
        Order cancelledOrder = new Order();
        cancelledOrder.setUser(testOrder.getUser());
        cancelledOrder.setOrderDate(LocalDateTime.now());
        cancelledOrder.setStatus(Order.OrderStatus.CANCELLED);
        cancelledOrder.setShippingAddress(testOrder.getShippingAddress());
        cancelledOrder.setBillingAddress(testOrder.getBillingAddress());
        cancelledOrder.setSubtotal(new BigDecimal("99.99"));
        cancelledOrder.setShippingCost(new BigDecimal("10.00"));
        cancelledOrder.setTax(new BigDecimal("5.00"));
        cancelledOrder.setDiscount(BigDecimal.ZERO);
        cancelledOrder.setTotal(new BigDecimal("114.99"));
        cancelledOrder = orderRepository.save(cancelledOrder);

        OrderItem activeItem = createOrderItem(testProduct1, 10);
        orderItemRepository.save(activeItem);

        OrderItem cancelledItem = new OrderItem();
        cancelledItem.setOrder(cancelledOrder);
        cancelledItem.setProduct(testProduct1);
        cancelledItem.setQuantity(100);
        cancelledItem.setUnitPrice(testProduct1.getPrice());
        cancelledItem.setTotalPrice(testProduct1.getPrice().multiply(new BigDecimal("100")));
        orderItemRepository.save(cancelledItem);

        List<TopSellingProductDto> topProducts = orderItemRepository.findTopSellingProducts(10);

        assertThat(topProducts).isNotEmpty();
        TopSellingProductDto topProduct = topProducts.stream()
                .filter(p -> p.getProductId().equals(testProduct1.getId()))
                .findFirst()
                .orElse(null);

        assertThat(topProduct).isNotNull();
        assertThat(topProduct.getTotalQuantitySold()).isEqualTo(10L);
    }

    @Test
    void save_shouldCalculateTotalPrice() {
        OrderItem orderItem = createOrderItem(testProduct1, 3);
        BigDecimal expectedTotal = testProduct1.getPrice().multiply(new BigDecimal("3"));

        OrderItem saved = orderItemRepository.save(orderItem);

        assertThat(saved.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }

    private OrderItem createOrderItem(Product product, Integer quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(testOrder);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(product.getPrice());
        orderItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(quantity)));
        return orderItem;
    }
}