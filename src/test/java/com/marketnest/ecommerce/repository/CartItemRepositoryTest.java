package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Cart;
import com.marketnest.ecommerce.model.CartItem;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Cart testCart;
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
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

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

        testCart = new Cart();
        testCart.setUserId(1L);
        testCart.setStatus(Cart.CartStatus.ACTIVE);
        testCart.setTotalPrice(BigDecimal.ZERO);
        testCart = cartRepository.save(testCart);
    }

    @Test
    void save_shouldPersistCartItem() {
        CartItem cartItem = createCartItem(testCart, testProduct, 2);

        CartItem saved = cartItemRepository.save(cartItem);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(2);
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void countByCartId_shouldReturnCorrectCount() {
        CartItem item1 = createCartItem(testCart, testProduct, 1);
        CartItem item2 = createCartItem(testCart, testProduct, 2);

        cartItemRepository.save(item1);
        cartItemRepository.save(item2);

        int count = cartItemRepository.countByCartId(testCart.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByCartId_shouldReturnZero_whenNoItems() {
        int count = cartItemRepository.countByCartId(testCart.getId());

        assertThat(count).isZero();
    }

    @Test
    void deleteByCartIdAndId_shouldRemoveSpecificItem() {
        CartItem item1 = createCartItem(testCart, testProduct, 1);
        CartItem item2 = createCartItem(testCart, testProduct, 2);

        CartItem saved1 = cartItemRepository.save(item1);
        cartItemRepository.save(item2);

        cartItemRepository.deleteByCartIdAndId(testCart.getId(), saved1.getId());

        int count = cartItemRepository.countByCartId(testCart.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getSubtotal_shouldCalculateCorrectly() {
        CartItem cartItem = createCartItem(testCart, testProduct, 3);
        cartItem.setPrice(new BigDecimal("10.00"));

        BigDecimal subtotal = cartItem.getSubtotal();

        assertThat(subtotal).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void incrementQuantity_shouldIncreaseQuantity() {
        CartItem cartItem = createCartItem(testCart, testProduct, 2);

        cartItem.incrementQuantity(3);

        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void decrementQuantity_shouldDecreaseQuantity() {
        CartItem cartItem = createCartItem(testCart, testProduct, 5);

        cartItem.decrementQuantity(2);

        assertThat(cartItem.getQuantity()).isEqualTo(3);
    }

    @Test
    void decrementQuantity_shouldNotGoBelowOne() {
        CartItem cartItem = createCartItem(testCart, testProduct, 2);

        cartItem.decrementQuantity(5);

        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }

    private CartItem createCartItem(Cart cart, Product product, Integer quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice());
        return cartItem;
    }
}