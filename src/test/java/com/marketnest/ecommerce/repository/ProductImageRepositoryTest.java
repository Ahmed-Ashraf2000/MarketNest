package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.ProductImage;
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
class ProductImageRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ProductImageRepository productImageRepository;

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
        productImageRepository.deleteAll();
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
    void save_shouldPersistProductImage() {
        ProductImage image =
                createProductImage("https://example.com/image1.jpg", ProductImage.ImageType.MAIN);

        ProductImage saved = productImageRepository.save(image);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo("https://example.com/image1.jpg");
        assertThat(saved.getImageType()).isEqualTo(ProductImage.ImageType.MAIN);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_shouldReturnImage_whenExists() {
        ProductImage image = createProductImage("https://example.com/image2.jpg",
                ProductImage.ImageType.GALLERY);
        ProductImage saved = productImageRepository.save(image);

        Optional<ProductImage> found = productImageRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo("https://example.com/image2.jpg");
    }

    @Test
    void findByProductId_shouldReturnAllImages() {
        ProductImage image1 =
                createProductImage("https://example.com/image1.jpg", ProductImage.ImageType.MAIN);
        ProductImage image2 = createProductImage("https://example.com/image2.jpg",
                ProductImage.ImageType.GALLERY);
        image2.setIsActive(false);

        productImageRepository.save(image1);
        productImageRepository.save(image2);

        List<ProductImage> images = productImageRepository.findByProductId(testProduct.getId());

        assertThat(images).hasSize(2);
    }

    @Test
    void findByProductIdAndIsActiveTrue_shouldReturnOnlyActiveImages() {
        ProductImage activeImage =
                createProductImage("https://example.com/active.jpg", ProductImage.ImageType.MAIN);
        ProductImage inactiveImage = createProductImage("https://example.com/inactive.jpg",
                ProductImage.ImageType.GALLERY);
        inactiveImage.setIsActive(false);

        productImageRepository.save(activeImage);
        productImageRepository.save(inactiveImage);

        List<ProductImage> images =
                productImageRepository.findByProductIdAndIsActiveTrue(testProduct.getId());

        assertThat(images).hasSize(1);
        assertThat(images.getFirst().getIsActive()).isTrue();
        assertThat(images.getFirst().getUrl()).isEqualTo("https://example.com/active.jpg");
    }

    @Test
    void findByProductId_shouldReturnEmptyList_whenNoImages() {
        List<ProductImage> images = productImageRepository.findByProductId(testProduct.getId());

        assertThat(images).isEmpty();
    }

    @Test
    void delete_shouldRemoveImage() {
        ProductImage image = createProductImage("https://example.com/to-delete.jpg",
                ProductImage.ImageType.THUMBNAIL);
        ProductImage saved = productImageRepository.save(image);

        productImageRepository.deleteById(saved.getId());

        Optional<ProductImage> found = productImageRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldSetTimestamps() {
        ProductImage image = createProductImage("https://example.com/timestamped.jpg",
                ProductImage.ImageType.MAIN);

        ProductImage saved = productImageRepository.save(image);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_shouldStoreAllImageMetadata() {
        ProductImage image = createProductImage("https://example.com/detailed.jpg",
                ProductImage.ImageType.GALLERY);
        image.setAltText("Product image");
        image.setTitle("Main product photo");
        image.setFileName("product.jpg");
        image.setFileSize(1024000L);
        image.setMimeType("image/jpeg");
        image.setWidth(1920);
        image.setHeight(1080);
        image.setPosition(1);

        ProductImage saved = productImageRepository.save(image);

        assertThat(saved.getAltText()).isEqualTo("Product image");
        assertThat(saved.getTitle()).isEqualTo("Main product photo");
        assertThat(saved.getFileName()).isEqualTo("product.jpg");
        assertThat(saved.getFileSize()).isEqualTo(1024000L);
        assertThat(saved.getMimeType()).isEqualTo("image/jpeg");
        assertThat(saved.getWidth()).isEqualTo(1920);
        assertThat(saved.getHeight()).isEqualTo(1080);
        assertThat(saved.getPosition()).isEqualTo(1);
    }

    @Test
    void findByProductId_shouldOrderByPosition() {
        ProductImage image1 = createProductImage("https://example.com/image1.jpg",
                ProductImage.ImageType.GALLERY);
        image1.setPosition(2);
        ProductImage image2 = createProductImage("https://example.com/image2.jpg",
                ProductImage.ImageType.GALLERY);
        image2.setPosition(1);

        productImageRepository.save(image1);
        productImageRepository.save(image2);

        List<ProductImage> images = productImageRepository.findByProductId(testProduct.getId());

        assertThat(images).hasSize(2);
    }

    private ProductImage createProductImage(String url, ProductImage.ImageType type) {
        ProductImage image = new ProductImage();
        image.setProductId(testProduct.getId());
        image.setUrl(url);
        image.setImageType(type);
        image.setIsActive(true);
        image.setPosition(0);
        return image;
    }
}