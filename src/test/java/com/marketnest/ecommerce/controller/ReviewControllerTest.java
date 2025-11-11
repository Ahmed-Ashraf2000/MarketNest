package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.review.CreateReviewRequest;
import com.marketnest.ecommerce.dto.review.ReviewResponse;
import com.marketnest.ecommerce.dto.review.UpdateReviewRequest;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.ReviewNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.review.ReviewService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private ReviewResponse reviewResponse;
    private CreateReviewRequest createRequest;
    private UpdateReviewRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setRating(5);
        reviewResponse.setTitle("Great product");
        reviewResponse.setComment("Love it!");

        createRequest = new CreateReviewRequest();
        createRequest.setRating(5);
        createRequest.setTitle("Great product");
        createRequest.setComment("Love it!");

        updateRequest = new UpdateReviewRequest();
        updateRequest.setRating(4);
        updateRequest.setTitle("Updated title");
        updateRequest.setComment("Updated comment");
    }

    @Test
    void getProductReviews_shouldReturnReviews() throws Exception {
        Page<ReviewResponse> reviewPage = new PageImpl<>(Collections.singletonList(reviewResponse));

        when(reviewService.getProductReviews(eq(1L), any()))
                .thenReturn(reviewPage);

        mockMvc.perform(get("/api/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating", is(5)));

        verify(reviewService).getProductReviews(eq(1L), any());
    }

    @Test
    void getReviewDetails_shouldReturnReview() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(reviewResponse);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rating", is(5)));

        verify(reviewService).getReviewById(1L);
    }

    @Test
    void getReviewDetails_shouldReturn404_whenNotFound() throws Exception {
        when(reviewService.getReviewById(anyLong()))
                .thenThrow(new ReviewNotFoundException("Review not found"));

        mockMvc.perform(get("/api/reviews/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addReview_shouldReturnCreatedReview() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(reviewService.createReview(eq(1L), eq(1L), any(CreateReviewRequest.class)))
                .thenReturn(reviewResponse);

        mockMvc.perform(post("/api/products/1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.rating", is(5)));

        verify(reviewService).createReview(eq(1L), eq(1L), any(CreateReviewRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addReview_shouldReturn400_whenValidationFails() throws Exception {
        CreateReviewRequest invalidRequest = new CreateReviewRequest();
        invalidRequest.setRating(6); // Invalid rating

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/products/1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).createReview(anyLong(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addReview_shouldReturn404_whenProductNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(reviewService.createReview(anyLong(), anyLong(), any()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/products/999/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addReview_shouldReturn409_whenDuplicateReview() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(reviewService.createReview(anyLong(), anyLong(), any()))
                .thenThrow(new BadRequestException("Already reviewed"));

        mockMvc.perform(post("/api/products/1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateReview_shouldReturnUpdatedReview() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(reviewService.updateReview(eq(1L), eq(1L), any(UpdateReviewRequest.class)))
                .thenReturn(reviewResponse);

        mockMvc.perform(put("/api/reviews/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(reviewService).updateReview(eq(1L), eq(1L), any(UpdateReviewRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteReview_shouldReturnNoContent() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        doNothing().when(reviewService).deleteReview(1L, 1L);

        mockMvc.perform(delete("/api/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(1L, 1L);
    }

    @Test
    @WithMockUser
    void markReviewAsHelpful_shouldReturnOk() throws Exception {
        doNothing().when(reviewService).markReviewAsHelpful(1L);

        mockMvc.perform(post("/api/reviews/1/helpful")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reviewService).markReviewAsHelpful(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReviewByAdmin_shouldReturnNoContent() throws Exception {
        doNothing().when(reviewService).deleteReviewByAdmin(1L);

        mockMvc.perform(delete("/api/admin/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReviewByAdmin(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveReview_shouldReturnApprovedReview() throws Exception {
        when(reviewService.approveReview(1L)).thenReturn(reviewResponse);

        mockMvc.perform(patch("/api/admin/reviews/1/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(reviewService).approveReview(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectReview_shouldReturnRejectedReview() throws Exception {
        when(reviewService.rejectReview(1L)).thenReturn(reviewResponse);

        mockMvc.perform(patch("/api/admin/reviews/1/reject")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(reviewService).rejectReview(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReviews_shouldReturnAllReviews() throws Exception {
        Page<ReviewResponse> reviewPage = new PageImpl<>(Collections.singletonList(reviewResponse));

        when(reviewService.getAllReviews(any())).thenReturn(reviewPage);

        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(reviewService).getAllReviews(any());
    }

    @Test
    @WithMockUser(username = "notfound@example.com")
    void addReview_shouldReturn404_whenUserNotFound() throws Exception {
        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/products/1/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        verify(reviewService, never()).createReview(anyLong(), anyLong(), any());
    }
}