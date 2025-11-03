package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.Review;
import com.marketnest.ecommerce.model.User;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product testProduct;
    private User testUser;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
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

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser = userRepository.save(testUser);
    }

    @Test
    void save_shouldPersistReview() {
        Review review = createReview(5, "Great product", "Love it!", true, true);

        Review saved = reviewRepository.save(review);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRating()).isEqualTo(5);
        assertThat(saved.getTitle()).isEqualTo("Great product");
        assertThat(saved.getComment()).isEqualTo("Love it!");
        assertThat(saved.getVerifiedPurchase()).isTrue();
        assertThat(saved.getIsApproved()).isTrue();
        assertThat(saved.getHelpfulCount()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByProductIdAndIsApprovedTrue_shouldReturnApprovedReviews() {
        Review approvedReview = createReview(5, "Great", "Approved", false, true);
        Review pendingReview = createReview(4, "Good", "Not approved", false, false);

        reviewRepository.save(approvedReview);
        reviewRepository.save(pendingReview);

        Page<Review> reviews = reviewRepository.findByProductIdAndIsApprovedTrue(
                testProduct.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(reviews.getContent()).hasSize(1);
        assertThat(reviews.getContent().getFirst().getIsApproved()).isTrue();
        assertThat(reviews.getContent().getFirst().getTitle()).isEqualTo("Great");
    }

    @Test
    void findByProductIdAndIsApprovedTrue_shouldReturnEmptyPage_whenNoApprovedReviews() {
        Review pendingReview = createReview(4, "Good", "Not approved", false, false);
        reviewRepository.save(pendingReview);

        Page<Review> reviews = reviewRepository.findByProductIdAndIsApprovedTrue(
                testProduct.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(reviews.getContent()).isEmpty();
    }

    @Test
    void findByIdAndUser_UserId_shouldReturnReview_whenExists() {
        Review review = createReview(5, "Great", "Excellent", false, true);
        Review saved = reviewRepository.save(review);

        Optional<Review> found = reviewRepository.findByIdAndUser_UserId(
                saved.getId(),
                testUser.getUserId()
        );

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void findByIdAndUser_UserId_shouldReturnEmpty_whenDifferentUser() {
        Review review = createReview(5, "Great", "Excellent", false, true);
        Review saved = reviewRepository.save(review);

        Optional<Review> found = reviewRepository.findByIdAndUser_UserId(saved.getId(), 999L);

        assertThat(found).isEmpty();
    }

    @Test
    void existsByProductIdAndUser_UserId_shouldReturnTrue_whenReviewExists() {
        Review review = createReview(5, "Great", "Excellent", false, true);
        reviewRepository.save(review);

        boolean exists = reviewRepository.existsByProductIdAndUser_UserId(
                testProduct.getId(),
                testUser.getUserId()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByProductIdAndUser_UserId_shouldReturnFalse_whenNoReview() {
        boolean exists = reviewRepository.existsByProductIdAndUser_UserId(
                testProduct.getId(),
                999L
        );

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldUpdateExistingReview() {
        Review review = createReview(3, "Okay", "Average", false, false);
        Review saved = reviewRepository.save(review);

        saved.setRating(5);
        saved.setTitle("Much better");
        saved.setComment("Changed my mind");
        saved.setIsApproved(true);

        Review updated = reviewRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getRating()).isEqualTo(5);
        assertThat(updated.getTitle()).isEqualTo("Much better");
        assertThat(updated.getIsApproved()).isTrue();
    }

    @Test
    void save_shouldIncrementHelpfulCount() {
        Review review = createReview(5, "Great", "Excellent", false, true);
        Review saved = reviewRepository.save(review);

        saved.setHelpfulCount(saved.getHelpfulCount() + 1);
        Review updated = reviewRepository.save(saved);

        assertThat(updated.getHelpfulCount()).isEqualTo(1);
    }

    @Test
    void delete_shouldRemoveReview() {
        Review review = createReview(5, "Great", "Excellent", false, true);
        Review saved = reviewRepository.save(review);

        reviewRepository.deleteById(saved.getId());

        Optional<Review> found = reviewRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldHandleAllRatingValues() {
        for (int rating = 1; rating <= 5; rating++) {
            Review review = createReview(rating, "Title", "Comment", false, true);
            Review saved = reviewRepository.save(review);

            assertThat(saved.getRating()).isEqualTo(rating);
        }
    }

    @Test
    void save_shouldAllowNullTitle() {
        Review review = createReview(5, null, "Comment", false, true);

        Review saved = reviewRepository.save(review);

        assertThat(saved.getTitle()).isNull();
        assertThat(saved.getComment()).isEqualTo("Comment");
    }

    @Test
    void save_shouldAllowNullComment() {
        Review review = createReview(5, "Title", null, false, true);

        Review saved = reviewRepository.save(review);

        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getComment()).isNull();
    }

    @Test
    void findByProductIdAndIsApprovedTrue_shouldRespectPagination() {
        for (int i = 0; i < 15; i++) {
            Review review = createReview(5, "Title " + i, "Comment " + i, false, true);
            reviewRepository.save(review);
        }

        Page<Review> page1 = reviewRepository.findByProductIdAndIsApprovedTrue(
                testProduct.getId(),
                PageRequest.of(0, 10)
        );
        Page<Review> page2 = reviewRepository.findByProductIdAndIsApprovedTrue(
                testProduct.getId(),
                PageRequest.of(1, 10)
        );

        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(15);
    }

    private Review createReview(Integer rating, String title, String comment,
                                boolean verifiedPurchase, boolean approved) {
        Review review = new Review();
        review.setProduct(testProduct);
        review.setUser(testUser);
        review.setRating(rating);
        review.setTitle(title);
        review.setComment(comment);
        review.setVerifiedPurchase(verifiedPurchase);
        review.setIsApproved(approved);
        review.setHelpfulCount(0);
        return review;
    }
}