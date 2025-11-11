package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.cart.CartItemRequest;
import com.marketnest.ecommerce.dto.cart.CartResponse;
import com.marketnest.ecommerce.dto.cart.UpdateCartItemRequest;
import com.marketnest.ecommerce.exception.CartNotFoundException;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.cart.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        cartResponse = new CartResponse();
        cartResponse.setId(1L);
        cartResponse.setUserId(1L);
        cartResponse.setTotalPrice(new BigDecimal("199.98"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserCart_shouldReturnCart() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.getUserCart(1L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.totalPrice", is(199.98)));

        verify(cartService).getUserCart(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToCart_shouldReturnCreatedCart() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(eq(1L), any(CartItemRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));

        verify(cartService).addToCart(eq(1L), any(CartItemRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToCart_shouldReturn400_whenValidationFails() throws Exception {
        CartItemRequest request = new CartItemRequest();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addToCart_shouldReturn404_whenProductNotFound() throws Exception {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(999L);
        request.setQuantity(1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(anyLong(), any(CartItemRequest.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/cart/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCartItemQuantity_shouldReturnUpdatedCart() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.updateCartItemQuantity(1L, 1L, 5)).thenReturn(cartResponse);

        mockMvc.perform(patch("/api/cart/items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(cartService).updateCartItemQuantity(1L, 1L, 5);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCartItemQuantity_shouldReturn400_whenQuantityInvalid() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(0);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(patch("/api/cart/items/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCartItemQuantity_shouldReturn404_whenItemNotFound() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.updateCartItemQuantity(anyLong(), anyLong(), anyInt()))
                .thenThrow(new CartNotFoundException("Cart item not found"));

        mockMvc.perform(patch("/api/cart/items/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeCartItem_shouldReturnUpdatedCart() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.removeCartItem(1L, 1L)).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/cart/items/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(cartService).removeCartItem(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void removeCartItem_shouldReturn404_whenItemNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.removeCartItem(anyLong(), anyLong()))
                .thenThrow(new CartNotFoundException("Cart item not found"));

        mockMvc.perform(delete("/api/cart/items/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void clearCart_shouldReturnEmptyCart() throws Exception {
        cartResponse.setTotalPrice(BigDecimal.ZERO);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.clearCart(1L)).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/cart")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice", is(0)));

        verify(cartService).clearCart(1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void clearCart_shouldReturn404_whenCartNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.clearCart(anyLong()))
                .thenThrow(new CategoryNotFoundException("Active cart not found"));

        mockMvc.perform(delete("/api/cart")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCartItemsCount_shouldReturnCount() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartService.getCartItemsCount(1L)).thenReturn(5);

        mockMvc.perform(get("/api/cart/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));

        verify(cartService).getCartItemsCount(1L);
    }
}