package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.order.OrderRequestDto;
import com.marketnest.ecommerce.dto.order.OrderResponseDto;
import com.marketnest.ecommerce.dto.order.OrderSummaryDto;
import com.marketnest.ecommerce.exception.OrderNotFoundException;
import com.marketnest.ecommerce.model.Order;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.order.OrderService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;
    private OrderResponseDto orderResponseDto;
    private OrderSummaryDto orderSummaryDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(1L);
        orderResponseDto.setStatus(Order.OrderStatus.PENDING.toString());
        orderResponseDto.setTotal(new BigDecimal("114.99"));

        orderSummaryDto = new OrderSummaryDto();
        orderSummaryDto.setId(1L);
        orderSummaryDto.setOrderDate(LocalDateTime.now());
        orderSummaryDto.setStatus(Order.OrderStatus.PENDING.toString());
        orderSummaryDto.setTotal(new BigDecimal("114.99"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserOrders_shouldReturnOrders() throws Exception {
        Page<OrderSummaryDto> page = new PageImpl<>(Collections.singletonList(orderSummaryDto));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.getUserOrders(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].total", is(114.99)));

        verify(orderService).getUserOrders(eq(1L), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderDetails_shouldReturnOrderDetails() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.getOrderDetails(1L, 1L)).thenReturn(orderResponseDto);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.total", is(114.99)));

        verify(orderService).getOrderDetails(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getOrderDetails_shouldReturn404_whenOrderNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.getOrderDetails(anyLong(), anyLong()))
                .thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setShippingAddressId(1L);
        requestDto.setBillingAddressId(1L);

        when(orderService.createOrder(any(OrderRequestDto.class))).thenReturn(orderResponseDto);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(orderService).createOrder(any(OrderRequestDto.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createOrder_shouldReturn400_whenValidationFails() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto();

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelOrder_shouldReturnCancelledOrder() throws Exception {
        orderResponseDto.setStatus(Order.OrderStatus.CANCELLED.toString());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.cancelOrder(1L, 1L)).thenReturn(orderResponseDto);

        mockMvc.perform(post("/api/orders/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(orderService).cancelOrder(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void cancelOrder_shouldReturn404_whenOrderNotFound() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.cancelOrder(anyLong(), anyLong()))
                .thenThrow(new OrderNotFoundException("Order not found"));

        mockMvc.perform(post("/api/orders/999/cancel")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void trackOrder_shouldReturnOrderTracking() throws Exception {
        orderResponseDto.setTrackingNumber("TRACK123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.trackOrder(1L, 1L)).thenReturn(orderResponseDto);

        mockMvc.perform(get("/api/orders/1/track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.trackingNumber", is("TRACK123")));

        verify(orderService).trackOrder(1L, 1L);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void downloadInvoice_shouldReturnPdf() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderService.generateInvoicePdf(1L, 1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/orders/1/invoice"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(pdfBytes));

        verify(orderService).generateInvoicePdf(1L, 1L);
    }

    @Test
    @WithMockUser(username = "notfound@example.com")
    void getUserOrders_shouldReturn404_whenUserNotFound() throws Exception {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isNotFound());

        verify(orderService, never()).getUserOrders(anyLong(), any());
    }
}