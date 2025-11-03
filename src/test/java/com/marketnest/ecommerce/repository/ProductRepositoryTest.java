package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.dto.analytics.LowStockProductDto;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.ProductImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    private Category testCategory;
    private Product testProduct;
    private Pageable pageable;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        productImageRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory = categoryRepository.save(testCategory);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setSlug("test-product");
        testProduct.setDescription("Test Description");
        testProduct.setSku("SKU-001");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStockQuantity(100);
        testProduct.setLowStockThreshold(10);
        testProduct.setCategory(testCategory);
        testProduct.setIsActive(true);
        testProduct.setIsFeatured(false);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByCategoryId_shouldReturnProducts() {
        productRepository.save(testProduct);

        Page<Product> products = productRepository.findByCategoryId(testCategory.getId(), pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("Test Product");
    }

    @Test
    void findByCategoryIdAndIsActiveTrue_shouldReturnOnlyActiveProducts() {
        productRepository.save(testProduct);

        Product inactiveProduct = new Product();
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setSlug("inactive-product");
        inactiveProduct.setSku("SKU-002");
        inactiveProduct.setPrice(new BigDecimal("49.99"));
        inactiveProduct.setStockQuantity(50);
        inactiveProduct.setCategory(testCategory);
        inactiveProduct.setIsActive(false);
        productRepository.save(inactiveProduct);

        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(
                testCategory.getId(), pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getIsActive()).isTrue();
    }

    @Test
    void findByIsActiveTrue_shouldReturnOnlyActiveProducts() {
        productRepository.save(testProduct);

        Product inactiveProduct = new Product();
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setSlug("inactive-product");
        inactiveProduct.setSku("SKU-002");
        inactiveProduct.setPrice(new BigDecimal("49.99"));
        inactiveProduct.setStockQuantity(50);
        inactiveProduct.setCategory(testCategory);
        inactiveProduct.setIsActive(false);
        productRepository.save(inactiveProduct);

        Page<Product> products = productRepository.findByIsActiveTrue(pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("Test Product");
    }

    @Test
    void findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase_shouldReturnMatchingProducts() {
        productRepository.save(testProduct);

        Page<Product> products = productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "test", "test", pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("Test Product");
    }

    @Test
    void findByIsActiveTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase_shouldReturnActiveMatchingProducts() {
        productRepository.save(testProduct);

        Page<Product> products = productRepository
                .findByIsActiveTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "test", "test", pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getIsActive()).isTrue();
    }

    @Test
    void findBySlug_shouldReturnProduct_whenExists() {
        productRepository.save(testProduct);

        Optional<Product> found = productRepository.findBySlug("test-product");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
    }

    @Test
    void findBySlug_shouldReturnEmpty_whenNotExists() {
        Optional<Product> found = productRepository.findBySlug("non-existent");

        assertThat(found).isEmpty();
    }

    @Test
    void findByCategoryIdAndIdNot_shouldExcludeSpecifiedProduct() {
        Product savedProduct = productRepository.save(testProduct);

        Product anotherProduct = new Product();
        anotherProduct.setName("Another Product");
        anotherProduct.setSlug("another-product");
        anotherProduct.setSku("SKU-003");
        anotherProduct.setPrice(new BigDecimal("79.99"));
        anotherProduct.setStockQuantity(75);
        anotherProduct.setCategory(testCategory);
        anotherProduct.setIsActive(true);
        productRepository.save(anotherProduct);

        Page<Product> products = productRepository.findByCategoryIdAndIdNot(
                testCategory.getId(), savedProduct.getId(), pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("Another Product");
    }

    @Test
    void findByIsActiveTrueOrderByCreatedAtDesc_shouldReturnProductsInDescendingOrder() {
        productRepository.save(testProduct);

        Product newerProduct = new Product();
        newerProduct.setName("Newer Product");
        newerProduct.setSlug("newer-product");
        newerProduct.setSku("SKU-004");
        newerProduct.setPrice(new BigDecimal("129.99"));
        newerProduct.setStockQuantity(60);
        newerProduct.setCategory(testCategory);
        newerProduct.setIsActive(true);
        productRepository.save(newerProduct);

        Page<Product> products = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        assertThat(products.getContent()).hasSize(2);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("Newer Product");
    }

    @Test
    void findByIsFeaturedTrue_shouldReturnFeaturedProducts() {
        testProduct.setIsFeatured(true);
        productRepository.save(testProduct);

        Product nonFeaturedProduct = new Product();
        nonFeaturedProduct.setName("Non-Featured");
        nonFeaturedProduct.setSlug("non-featured");
        nonFeaturedProduct.setSku("SKU-005");
        nonFeaturedProduct.setPrice(new BigDecimal("39.99"));
        nonFeaturedProduct.setStockQuantity(40);
        nonFeaturedProduct.setCategory(testCategory);
        nonFeaturedProduct.setIsActive(true);
        nonFeaturedProduct.setIsFeatured(false);
        productRepository.save(nonFeaturedProduct);

        Page<Product> products = productRepository.findByIsFeaturedTrue(pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().getFirst().getIsFeatured()).isTrue();
    }

    @Test
    void countLowStockProducts_shouldReturnCorrectCount() {
        testProduct.setStockQuantity(5);
        testProduct.setLowStockThreshold(10);
        productRepository.save(testProduct);

        Long count = productRepository.countLowStockProducts();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findLowStockProducts_shouldReturnLowStockProductsWithImages() {
        testProduct.setStockQuantity(5);
        testProduct.setLowStockThreshold(10);
        Product savedProduct = productRepository.save(testProduct);

        ProductImage image = new ProductImage();
        image.setProduct(savedProduct);
        image.setUrl("http://example.com/image.jpg");
        productImageRepository.save(image);

        List<LowStockProductDto> lowStockProducts = productRepository.findLowStockProducts();

        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.getFirst().getProductName()).isEqualTo("Test Product");
        assertThat(lowStockProducts.getFirst().getCurrentStock()).isEqualTo(5);
        assertThat(lowStockProducts.getFirst().getImageUrl()).isEqualTo(
                "http://example.com/image.jpg");
    }

    @Test
    void findLowStockProducts_shouldReturnEmpty_whenNoLowStockProducts() {
        testProduct.setStockQuantity(50);
        testProduct.setLowStockThreshold(10);
        productRepository.save(testProduct);

        List<LowStockProductDto> lowStockProducts = productRepository.findLowStockProducts();

        assertThat(lowStockProducts).isEmpty();
    }
}