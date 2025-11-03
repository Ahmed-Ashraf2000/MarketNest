package com.marketnest.ecommerce.service.review;

import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.ReviewNotFoundException;
import com.marketnest.ecommerce.exception.UserNotFoundException;
import com.marketnest.ecommerce.mapper.review.ReviewMapper;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.Review;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.OrderRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.ReviewRepository;
import com.marketnest.ecommerce.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Product testProduct;
    private User testUser;
    private Review testReview;
    private CreateReviewRequest createRequest;
    private UpdateReviewRequest updateRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        testReview = new Review();
        testReview.setId(1L);
        testReview.setProduct(testProduct);
        testReview.setUser(testUser);
        testReview.setRating(5);
        testReview.setTitle("Great product");
        testReview.setComment("Love it!");
        testReview.setIsApproved(true);
        testReview.setVerifiedPurchase(false);
        testReview.setHelpfulCount(0);

        createRequest = new CreateReviewRequest();
        createRequest.setRating(5);
        createRequest.setTitle("Great product");
        createRequest.setComment("Love it!");

        updateRequest = new UpdateReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setTitle("Updated title");
        updateRequest.setComment("Updated comment");

        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setRating(5);
        reviewResponse.setTitle("Great product");
    }

    @Test
    void getProductReviews_shouldReturnApprovedReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(testReview));

        when(reviewRepository.findByProductIdAndIsApprovedTrue(1L, pageable))
                .thenReturn(reviewPage);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        Page<ReviewResponse> result = reviewService.getProductReviews(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
        verify(reviewRepository).findByProductIdAndIsApprovedTrue(1L, pageable);
    }

    @Test
    void getReviewById_shouldReturnReview_whenExists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.getReviewById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReviewById_shouldThrowException_whenNotFound() {
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(999L))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void createReview_shouldCreateReview_whenValid() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByProductIdAndUser_UserId(1L, 1L)).thenReturn(false);
        when(orderRepository.existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn(
                anyLong(), anyLong(), any())).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.createReview(1L, 1L, createRequest);

        assertThat(result).isNotNull();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(999L, 1L, createRequest))
                .isInstanceOf(ProductNotFoundException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowException_whenUserNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(1L, 999L, createRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldThrowException_whenAlreadyReviewed() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByProductIdAndUser_UserId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, 1L, createRequest))
                .isInstanceOf(BadRequestException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_shouldSetVerifiedPurchase_whenUserPurchasedProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByProductIdAndUser_UserId(1L, 1L)).thenReturn(false);
        when(orderRepository.existsByUser_UserIdAndOrderItems_Product_IdAndStatusIn(
                anyLong(), anyLong(), any())).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        reviewService.createReview(1L, 1L, createRequest);

        verify(reviewRepository).save(argThat(Review::getVerifiedPurchase));
    }

    @Test
    void updateReview_shouldUpdateReview_whenOwner() {
        when(reviewRepository.findByIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.updateReview(1L, 1L, updateRequest);

        assertThat(result).isNotNull();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateReview_shouldThrowException_whenNotOwner() {
        when(reviewRepository.findByIdAndUser_UserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(1L, 999L, updateRequest))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_shouldDeleteReview_whenOwner() {
        when(reviewRepository.findByIdAndUser_UserId(1L, 1L))
                .thenReturn(Optional.of(testReview));

        reviewService.deleteReview(1L, 1L);

        verify(reviewRepository).delete(testReview);
    }

    @Test
    void deleteReview_shouldThrowException_whenNotOwner() {
        when(reviewRepository.findByIdAndUser_UserId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(1L, 999L))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void markReviewAsHelpful_shouldIncrementCount() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        reviewService.markReviewAsHelpful(1L);

        verify(reviewRepository).save(argThat(review ->
                review.getHelpfulCount() == 1
        ));
    }

    @Test
    void deleteReviewByAdmin_shouldDeleteAnyReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        reviewService.deleteReviewByAdmin(1L);

        verify(reviewRepository).delete(testReview);
    }

    @Test
    void approveReview_shouldSetApprovalStatus() {
        testReview.setIsApproved(false);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.approveReview(1L);

        assertThat(result).isNotNull();
        verify(reviewRepository).save(argThat(Review::getIsApproved));
    }

    @Test
    void rejectReview_shouldSetApprovalStatus() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        ReviewResponse result = reviewService.rejectReview(1L);

        assertThat(result).isNotNull();
        verify(reviewRepository).save(argThat(review -> !review.getIsApproved()));
    }

    @Test
    void getAllReviews_shouldReturnAllReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(testReview));

        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);
        when(reviewMapper.toResponse(testReview)).thenReturn(reviewResponse);

        Page<ReviewResponse> result = reviewService.getAllReviews(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(reviewRepository).findAll(pageable);
    }
}