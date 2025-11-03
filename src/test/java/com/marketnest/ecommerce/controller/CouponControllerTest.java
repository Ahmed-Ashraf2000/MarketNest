package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.coupon.ApplyCouponRequest;
import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CouponValidationResponse;
import com.marketnest.ecommerce.model.Coupon;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.coupon.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private CouponResponse couponResponse;
    private CouponValidationResponse validationResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");

        couponResponse = new CouponResponse();
        couponResponse.setId(1L);
        couponResponse.setCode("TEST10");
        couponResponse.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        couponResponse.setDiscountValue(new BigDecimal("10"));

        validationResponse = CouponValidationResponse.builder()
                .valid(true)
                .discountAmount(new BigDecimal("10.00"))
                .message("Coupon applied successfully")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void validateCoupon_shouldReturnValidationResponse() throws Exception {
        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("TEST10");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(couponService.validateCoupon(eq("TEST10"), eq(1L), any(BigDecimal.class)))
                .thenReturn(validationResponse);

        mockMvc.perform(post("/api/coupons/validate")
                        .with(csrf())
                        .param("orderAmount", "100.00")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.discountAmount", is(10.00)));

        verify(couponService).validateCoupon(eq("TEST10"), eq(1L), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void validateCoupon_shouldReturn400_whenOrderAmountInvalid() throws Exception {
        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("TEST10");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/coupons/validate")
                        .with(csrf())
                        .param("orderAmount", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(couponService, never()).validateCoupon(any(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAvailableCoupons_shouldReturnCouponsList() throws Exception {
        List<CouponResponse> coupons = Collections.singletonList(couponResponse);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(couponService.getAvailableCoupons(1L)).thenReturn(coupons);

        mockMvc.perform(get("/api/coupons/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code", is("TEST10")));

        verify(couponService).getAvailableCoupons(1L);
    }

    @Test
    @WithMockUser(username = "notfound@example.com")
    void validateCoupon_shouldReturn404_whenUserNotFound() throws Exception {
        ApplyCouponRequest request = new ApplyCouponRequest();
        request.setCode("TEST10");

        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/coupons/validate")
                        .with(csrf())
                        .param("orderAmount", "100.00")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(couponService, never()).validateCoupon(any(), anyLong(), any());
    }
}