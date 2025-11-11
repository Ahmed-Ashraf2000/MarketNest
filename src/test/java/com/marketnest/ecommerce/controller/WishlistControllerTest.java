package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.wishlist.AddWishlistItemRequest;
import com.marketnest.ecommerce.dto.wishlist.WishlistResponse;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.WishlistNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.model.WishlistItem;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.wishlist.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private WishlistResponse wishlistResponse;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        wishlistResponse = new WishlistResponse();
        wishlistResponse.setUserId(1L);
        wishlistResponse.setTotalItems(1);

        cartResponse = new CartResponse();
        cartResponse.setUserId(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserWishlist_shouldReturnWishlist() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.getUserWishlist(1L)).thenReturn(wishlistResponse);

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.totalItems", is(1)));

        verify(wishlistService).getUserWishlist(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToWishlist_shouldReturnUpdatedWishlist() throws Exception {
        AddWishlistItemRequest request = new AddWishlistItemRequest();
        request.setProductId(1L);
        request.setPriority(WishlistItem.Priority.HIGH);
        request.setNotes("Test notes");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.addToWishlist(eq(1L), any(AddWishlistItemRequest.class)))
                .thenReturn(wishlistResponse);

        mockMvc.perform(post("/api/wishlist/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)));

        verify(wishlistService).addToWishlist(eq(1L), any(AddWishlistItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToWishlist_shouldReturn400_whenValidationFails() throws Exception {
        AddWishlistItemRequest request = new AddWishlistItemRequest();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/wishlist/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(wishlistService, never()).addToWishlist(anyLong(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToWishlist_shouldReturn404_whenProductNotFound() throws Exception {
        AddWishlistItemRequest request = new AddWishlistItemRequest();
        request.setProductId(999L);
        request.setPriority(WishlistItem.Priority.HIGH);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.addToWishlist(anyLong(), any(AddWishlistItemRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/wishlist/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeFromWishlist_shouldReturnUpdatedWishlist() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.removeFromWishlist(1L, 1L)).thenReturn(wishlistResponse);

        mockMvc.perform(delete("/api/wishlist/items/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)));

        verify(wishlistService).removeFromWishlist(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeFromWishlist_shouldReturn404_whenItemNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.removeFromWishlist(anyLong(), anyLong()))
                .thenThrow(new WishlistNotFoundException("Item not found"));

        mockMvc.perform(delete("/api/wishlist/items/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void clearWishlist_shouldReturnEmptyWishlist() throws Exception {
        wishlistResponse.setTotalItems(0);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.clearWishlist(1L)).thenReturn(wishlistResponse);

        mockMvc.perform(delete("/api/wishlist")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems", is(0)));

        verify(wishlistService).clearWishlist(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void moveToCart_shouldReturnCartResponse() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.moveToCart(1L, 1L)).thenReturn(cartResponse);

        mockMvc.perform(post("/api/wishlist/items/1/move-to-cart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)));

        verify(wishlistService).moveToCart(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void moveToCart_shouldReturn404_whenItemNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(wishlistService.moveToCart(anyLong(), anyLong()))
                .thenThrow(new WishlistNotFoundException("Item not found"));

        mockMvc.perform(post("/api/wishlist/items/999/move-to-cart")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "notfound@example.com")
    void getUserWishlist_shouldReturn404_whenUserNotFound() throws Exception {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isNotFound());

        verify(wishlistService, never()).getUserWishlist(anyLong());
    }
}