package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.ProductVariant;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductVariantRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ProductVariantRepository variantRepository;

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
        variantRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Clothing");
        category.setSlug("clothing");
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setName("T-Shirt");
        testProduct.setSlug("t-shirt");
        testProduct.setSku("SHIRT-001");
        testProduct.setPrice(new BigDecimal("29.99"));
        testProduct.setStockQuantity(100);
        testProduct.setCategory(category);
        testProduct.setIsActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void save_shouldPersistVariant() {
        ProductVariant variant = createVariant("SHIRT-001-RED-M", "Red", "M");

        ProductVariant saved = variantRepository.save(variant);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSku()).isEqualTo("SHIRT-001-RED-M");
        assertThat(saved.getOption1Value()).isEqualTo("Red");
        assertThat(saved.getOption2Value()).isEqualTo("M");
    }

    @Test
    void findById_shouldReturnVariant_whenExists() {
        ProductVariant variant = createVariant("SHIRT-001-BLUE-L", "Blue", "L");
        ProductVariant saved = variantRepository.save(variant);

        Optional<ProductVariant> found = variantRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSku()).isEqualTo("SHIRT-001-BLUE-L");
    }

    @Test
    void findByProductId_shouldReturnVariants() {
        ProductVariant variant1 = createVariant("SHIRT-001-RED-M", "Red", "M");
        ProductVariant variant2 = createVariant("SHIRT-001-BLUE-L", "Blue", "L");
        variantRepository.save(variant1);
        variantRepository.save(variant2);

        List<ProductVariant> variants = variantRepository.findAllById(List.of(testProduct.getId()));

        assertThat(variants).hasSize(2);
    }

    @Test
    void delete_shouldRemoveVariant() {
        ProductVariant variant = createVariant("SHIRT-001-GREEN-S", "Green", "S");
        ProductVariant saved = variantRepository.save(variant);

        variantRepository.deleteById(saved.getId());

        Optional<ProductVariant> found = variantRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void isInStock_shouldReturnTrue_whenStockAvailable() {
        ProductVariant variant = createVariant("SHIRT-001-BLACK-XL", "Black", "XL");
        variant.setStockQuantity(10);

        assertThat(variant.isInStock()).isTrue();
    }

    @Test
    void isInStock_shouldReturnFalse_whenStockZero() {
        ProductVariant variant = createVariant("SHIRT-001-WHITE-S", "White", "S");
        variant.setStockQuantity(0);

        assertThat(variant.isInStock()).isFalse();
    }

    @Test
    void isOnSale_shouldReturnTrue_whenCompareAtPriceHigher() {
        ProductVariant variant = createVariant("SHIRT-001-NAVY-M", "Navy", "M");
        variant.setPrice(new BigDecimal("19.99"));
        variant.setCompareAtPrice(new BigDecimal("29.99"));

        assertThat(variant.isOnSale()).isTrue();
    }

    @Test
    void isOnSale_shouldReturnFalse_whenNoCompareAtPrice() {
        ProductVariant variant = createVariant("SHIRT-001-GRAY-L", "Gray", "L");
        variant.setPrice(new BigDecimal("29.99"));

        assertThat(variant.isOnSale()).isFalse();
    }

    private ProductVariant createVariant(String sku, String color, String size) {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(testProduct.getId());
        variant.setSku(sku);
        variant.setOption1Name("Color");
        variant.setOption1Value(color);
        variant.setOption2Name("Size");
        variant.setOption2Value(size);
        variant.setPrice(new BigDecimal("29.99"));
        variant.setStockQuantity(50);
        variant.setIsAvailable(true);
        return variant;
    }
}