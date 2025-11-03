package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.payment.PaymentMethodDto;
import com.marketnest.ecommerce.dto.payment.PaymentProcessRequestDto;
import com.marketnest.ecommerce.dto.payment.PaymentResponseDto;
import com.marketnest.ecommerce.dto.payment.RefundRequestDto;
import com.marketnest.ecommerce.exception.OrderNotFoundException;
import com.marketnest.ecommerce.exception.PaymentNotFoundException;
import com.marketnest.ecommerce.model.Payment;
import com.marketnest.ecommerce.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentProcessRequestDto paymentProcessRequestDto;
    private PaymentResponseDto paymentResponseDto;
    private RefundRequestDto refundRequestDto;

    @BeforeEach
    void setUp() {
        paymentProcessRequestDto = new PaymentProcessRequestDto();
        paymentProcessRequestDto.setOrderId(1L);
        paymentProcessRequestDto.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD.toString());
        paymentProcessRequestDto.setStripeToken("tok_visa");

        paymentResponseDto = new PaymentResponseDto();
        paymentResponseDto.setId(1L);
        paymentResponseDto.setAmount(new BigDecimal("114.99"));
        paymentResponseDto.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD.toString());
        paymentResponseDto.setStatus(Payment.PaymentStatus.COMPLETED.toString());
        paymentResponseDto.setTransactionId("TXN123456");
        paymentResponseDto.setPaymentDate(LocalDateTime.now());

        refundRequestDto = new RefundRequestDto();
        refundRequestDto.setReason("Customer request");
        refundRequestDto.setAmount(new BigDecimal("114.99"));
    }

    @Test
    @WithMockUser
    void processPayment_shouldReturnCreatedPayment() throws Exception {
        when(paymentService.processPayment(any(PaymentProcessRequestDto.class)))
                .thenReturn(paymentResponseDto);

        mockMvc.perform(post("/api/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentProcessRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.amount", is(114.99)))
                .andExpect(jsonPath("$.paymentMethod", is("CREDIT_CARD")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.transactionId", is("TXN123456")));

        verify(paymentService).processPayment(any(PaymentProcessRequestDto.class));
    }

    @Test
    @WithMockUser
    void processPayment_shouldReturn400_whenValidationFails() throws Exception {
        PaymentProcessRequestDto invalidRequest = new PaymentProcessRequestDto();

        mockMvc.perform(post("/api/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processPayment(any());
    }

    @Test
    @WithMockUser
    void processPayment_shouldReturn404_whenOrderNotFound() throws Exception {
        when(paymentService.processPayment(any(PaymentProcessRequestDto.class)))
                .thenThrow(new OrderNotFoundException("Order not found with id: 999"));

        mockMvc.perform(post("/api/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentProcessRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getPaymentById_shouldReturnPayment() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResponseDto);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.amount", is(114.99)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        verify(paymentService).getPaymentById(1L);
    }

    @Test
    @WithMockUser
    void getPaymentById_shouldReturn404_whenPaymentNotFound() throws Exception {
        when(paymentService.getPaymentById(anyLong()))
                .thenThrow(new PaymentNotFoundException("Payment not found with id: 999"));

        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void refundPayment_shouldReturnRefundedPayment() throws Exception {
        paymentResponseDto.setStatus(Payment.PaymentStatus.REFUNDED.toString());

        when(paymentService.refundPayment(eq(1L), any(RefundRequestDto.class)))
                .thenReturn(paymentResponseDto);

        mockMvc.perform(post("/api/payments/1/refund")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("REFUNDED")));

        verify(paymentService).refundPayment(eq(1L), any(RefundRequestDto.class));
    }

    @Test
    @WithMockUser
    void refundPayment_shouldReturn400_whenValidationFails() throws Exception {
        RefundRequestDto invalidRequest = new RefundRequestDto();

        mockMvc.perform(post("/api/payments/1/refund")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).refundPayment(anyLong(), any());
    }

    @Test
    @WithMockUser
    void refundPayment_shouldReturn404_whenPaymentNotFound() throws Exception {
        when(paymentService.refundPayment(anyLong(), any(RefundRequestDto.class)))
                .thenThrow(new PaymentNotFoundException("Payment not found"));

        mockMvc.perform(post("/api/payments/999/refund")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getAvailablePaymentMethods_shouldReturnAllMethods() throws Exception {
        List<PaymentMethodDto> methods = Arrays.asList(
                new PaymentMethodDto("CREDIT_CARD", "Credit Card", "description", true),
                new PaymentMethodDto("DEBIT_CARD", "Debit Card", "description", true),
                new PaymentMethodDto("PAYPAL", "PayPal", "description", true),
                new PaymentMethodDto("BANK_TRANSFER", "Bank Transfer", "description", true),
                new PaymentMethodDto("CASH_ON_DELIVERY", "Cash on Delivery", "description", true)
        );

        when(paymentService.getAvailablePaymentMethods()).thenReturn(methods);

        mockMvc.perform(get("/api/payments/methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("CREDIT_CARD")))
                .andExpect(jsonPath("$[0].displayName", is("Credit Card")))
                .andExpect(jsonPath("$[0].enabled", is(true)));

        verify(paymentService).getAvailablePaymentMethods();
    }

    @Test
    @WithMockUser
    void processPayment_shouldHandleCashOnDelivery() throws Exception {
        paymentProcessRequestDto.setPaymentMethod(
                Payment.PaymentMethod.CASH_ON_DELIVERY.toString());
        paymentProcessRequestDto.setStripeToken(null);
        paymentResponseDto.setPaymentMethod(Payment.PaymentMethod.CASH_ON_DELIVERY.toString());
        paymentResponseDto.setStatus(Payment.PaymentStatus.PENDING.toString());

        when(paymentService.processPayment(any(PaymentProcessRequestDto.class)))
                .thenReturn(paymentResponseDto);

        mockMvc.perform(post("/api/payments/process")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentProcessRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod", is("CASH_ON_DELIVERY")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }
}