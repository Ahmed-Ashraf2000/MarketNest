package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.Wishlist;
import com.marketnest.ecommerce.model.WishlistItem;
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
class WishlistRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
        wishlistRepository.deleteAll();
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
    }

    @Test
    void save_shouldPersistWishlist() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);

        Wishlist saved = wishlistRepository.save(wishlist);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserId_shouldReturnWishlist_whenExists() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);
        wishlistRepository.save(wishlist);

        Optional<Wishlist> found = wishlistRepository.findByUserId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenNotExists() {
        Optional<Wishlist> found = wishlistRepository.findByUserId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldPersistWishlistWithItems() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);

        WishlistItem item = new WishlistItem();
        item.setProduct(testProduct);
        item.setPriority(WishlistItem.Priority.HIGH);
        item.setNotes("Test notes");

        wishlist.addWishlistItem(item);

        Wishlist saved = wishlistRepository.save(wishlist);

        assertThat(saved.getWishlistItems()).hasSize(1);
        assertThat(saved.getWishlistItems().getFirst().getProduct().getId())
                .isEqualTo(testProduct.getId());
    }

    @Test
    void delete_shouldRemoveWishlistAndItems() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);

        WishlistItem item = new WishlistItem();
        item.setProduct(testProduct);
        wishlist.addWishlistItem(item);

        Wishlist saved = wishlistRepository.save(wishlist);

        wishlistRepository.deleteById(saved.getId());

        Optional<Wishlist> found = wishlistRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void getTotalItems_shouldReturnCorrectCount() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);

        WishlistItem item1 = new WishlistItem();
        item1.setProduct(testProduct);
        wishlist.addWishlistItem(item1);

        WishlistItem item2 = new WishlistItem();
        item2.setProduct(testProduct);
        wishlist.addWishlistItem(item2);

        Wishlist saved = wishlistRepository.save(wishlist);

        assertThat(saved.getTotalItems()).isEqualTo(2);
    }

    @Test
    void containsProduct_shouldReturnTrue_whenProductExists() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);

        WishlistItem item = new WishlistItem();
        item.setProduct(testProduct);
        wishlist.addWishlistItem(item);

        Wishlist saved = wishlistRepository.save(wishlist);

        assertThat(saved.containsProduct(testProduct.getId())).isTrue();
    }

    @Test
    void containsProduct_shouldReturnFalse_whenProductNotExists() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);
        Wishlist saved = wishlistRepository.save(wishlist);

        assertThat(saved.containsProduct(999L)).isFalse();
    }

    @Test
    void removeWishlistItem_shouldRemoveItemFromList() {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(1L);

        WishlistItem item = new WishlistItem();
        item.setProduct(testProduct);
        wishlist.addWishlistItem(item);

        Wishlist saved = wishlistRepository.save(wishlist);
        WishlistItem savedItem = saved.getWishlistItems().getFirst();

        saved.removeWishlistItem(savedItem);
        wishlistRepository.save(saved);

        Wishlist updated = wishlistRepository.findById(saved.getId()).get();
        assertThat(updated.getWishlistItems()).isEmpty();
    }
}