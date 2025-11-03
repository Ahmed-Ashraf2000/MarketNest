package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Cart;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CartRepository cartRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void save_shouldPersistCart() {
        Cart cart = createCart(1L, Cart.CartStatus.ACTIVE);

        Cart saved = cartRepository.save(cart);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(Cart.CartStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserIdAndStatus_shouldReturnCart_whenExists() {
        Cart cart = createCart(1L, Cart.CartStatus.ACTIVE);
        cartRepository.save(cart);

        Optional<Cart> found = cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getStatus()).isEqualTo(Cart.CartStatus.ACTIVE);
    }

    @Test
    void findByUserIdAndStatus_shouldReturnEmpty_whenNotExists() {
        Optional<Cart> found = cartRepository.findByUserIdAndStatus(999L, Cart.CartStatus.ACTIVE);

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserIdAndStatus_shouldReturnEmpty_whenStatusDifferent() {
        Cart cart = createCart(1L, Cart.CartStatus.ACTIVE);
        cartRepository.save(cart);

        Optional<Cart> found = cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.COMPLETED);

        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldUpdateTotalPrice() {
        Cart cart = createCart(1L, Cart.CartStatus.ACTIVE);
        cart.setTotalPrice(new BigDecimal("99.99"));

        Cart saved = cartRepository.save(cart);

        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void save_shouldHandleMultipleCartsForDifferentUsers() {
        Cart cart1 = createCart(1L, Cart.CartStatus.ACTIVE);
        Cart cart2 = createCart(2L, Cart.CartStatus.ACTIVE);

        cartRepository.save(cart1);
        cartRepository.save(cart2);

        Optional<Cart> foundCart1 =
                cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE);
        Optional<Cart> foundCart2 =
                cartRepository.findByUserIdAndStatus(2L, Cart.CartStatus.ACTIVE);

        assertThat(foundCart1).isPresent();
        assertThat(foundCart2).isPresent();
        assertThat(foundCart1.get().getUserId()).isEqualTo(1L);
        assertThat(foundCart2.get().getUserId()).isEqualTo(2L);
    }

    @Test
    void save_shouldHandleMultipleStatusesForSameUser() {
        Cart activeCart = createCart(1L, Cart.CartStatus.ACTIVE);
        Cart completedCart = createCart(1L, Cart.CartStatus.COMPLETED);

        cartRepository.save(activeCart);
        cartRepository.save(completedCart);

        Optional<Cart> foundActive =
                cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.ACTIVE);
        Optional<Cart> foundCompleted =
                cartRepository.findByUserIdAndStatus(1L, Cart.CartStatus.COMPLETED);

        assertThat(foundActive).isPresent();
        assertThat(foundCompleted).isPresent();
    }

    @Test
    void delete_shouldRemoveCart() {
        Cart cart = createCart(1L, Cart.CartStatus.ACTIVE);
        Cart saved = cartRepository.save(cart);

        cartRepository.deleteById(saved.getId());

        Optional<Cart> found = cartRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    private Cart createCart(Long userId, Cart.CartStatus status) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(status);
        cart.setTotalPrice(BigDecimal.ZERO);
        return cart;
    }
}