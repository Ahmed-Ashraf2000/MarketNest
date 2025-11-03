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
class WishlistItemRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Wishlist testWishlist;
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
        wishlistItemRepository.deleteAll();
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

        testWishlist = new Wishlist();
        testWishlist.setUserId(1L);
        testWishlist = wishlistRepository.save(testWishlist);
    }

    @Test
    void save_shouldPersistWishlistItem() {
        WishlistItem item = createWishlistItem(WishlistItem.Priority.HIGH, "Test notes");

        WishlistItem saved = wishlistItemRepository.save(item);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProduct().getId()).isEqualTo(testProduct.getId());
        assertThat(saved.getWishlist().getId()).isEqualTo(testWishlist.getId());
        assertThat(saved.getPriority()).isEqualTo(WishlistItem.Priority.HIGH);
        assertThat(saved.getNotes()).isEqualTo("Test notes");
        assertThat(saved.getAddedAt()).isNotNull();
    }

    @Test
    void findById_shouldReturnWishlistItem_whenExists() {
        WishlistItem item = createWishlistItem(WishlistItem.Priority.MEDIUM, null);
        WishlistItem saved = wishlistItemRepository.save(item);

        Optional<WishlistItem> found = wishlistItemRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<WishlistItem> found = wishlistItemRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldHandleAllPriorityLevels() {
        for (WishlistItem.Priority priority : WishlistItem.Priority.values()) {
            WishlistItem item = createWishlistItem(priority, null);

            WishlistItem saved = wishlistItemRepository.save(item);

            assertThat(saved.getPriority()).isEqualTo(priority);
        }
    }

    @Test
    void save_shouldAllowNullNotes() {
        WishlistItem item = createWishlistItem(WishlistItem.Priority.LOW, null);

        WishlistItem saved = wishlistItemRepository.save(item);

        assertThat(saved.getNotes()).isNull();
    }

    @Test
    void save_shouldAllowLongNotes() {
        String longNotes = "A".repeat(1000);
        WishlistItem item = createWishlistItem(WishlistItem.Priority.HIGH, longNotes);

        WishlistItem saved = wishlistItemRepository.save(item);

        assertThat(saved.getNotes()).isEqualTo(longNotes);
    }

    @Test
    void delete_shouldRemoveWishlistItem() {
        WishlistItem item = createWishlistItem(WishlistItem.Priority.MEDIUM, null);
        WishlistItem saved = wishlistItemRepository.save(item);

        wishlistItemRepository.deleteById(saved.getId());

        Optional<WishlistItem> found = wishlistItemRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldSetDefaultPriority() {
        WishlistItem item = new WishlistItem();
        item.setWishlist(testWishlist);
        item.setProduct(testProduct);

        WishlistItem saved = wishlistItemRepository.save(item);

        assertThat(saved.getPriority()).isEqualTo(WishlistItem.Priority.MEDIUM);
    }

    @Test
    void save_shouldUpdateExistingItem() {
        WishlistItem item = createWishlistItem(WishlistItem.Priority.LOW, "Original notes");
        WishlistItem saved = wishlistItemRepository.save(item);

        saved.setPriority(WishlistItem.Priority.HIGH);
        saved.setNotes("Updated notes");

        WishlistItem updated = wishlistItemRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getPriority()).isEqualTo(WishlistItem.Priority.HIGH);
        assertThat(updated.getNotes()).isEqualTo("Updated notes");
    }

    private WishlistItem createWishlistItem(WishlistItem.Priority priority, String notes) {
        WishlistItem item = new WishlistItem();
        item.setWishlist(testWishlist);
        item.setProduct(testProduct);
        item.setPriority(priority);
        item.setNotes(notes);
        return item;
    }
}