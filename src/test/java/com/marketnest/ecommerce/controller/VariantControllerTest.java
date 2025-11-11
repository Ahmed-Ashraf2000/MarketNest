package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.variant.VariantRequestDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.VariantNotFoundException;
import com.marketnest.ecommerce.service.product.VariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VariantController.class)
class VariantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VariantService variantService;

    private VariantRequestDto requestDto;
    private VariantResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new VariantRequestDto();
        requestDto.setSku("SHIRT-001-RED-M");
        requestDto.setOption1Name("Color");
        requestDto.setOption1Value("Red");
        requestDto.setOption2Name("Size");
        requestDto.setOption2Value("M");
        requestDto.setPrice(new BigDecimal("29.99"));
        requestDto.setStockQuantity(50);

        responseDto = new VariantResponseDto();
        responseDto.setId(1L);
        responseDto.setSku("SHIRT-001-RED-M");
        responseDto.setOption1Name("Color");
        responseDto.setOption1Value("Red");
        responseDto.setOption2Name("Size");
        responseDto.setOption2Value("M");
        responseDto.setPrice(new BigDecimal("29.99"));
        responseDto.setStockQuantity(50);
    }

    @Test
    void getProductVariants_shouldReturnVariants() throws Exception {
        List<VariantResponseDto> variants = Collections.singletonList(responseDto);
        when(variantService.getVariantsByProductId(1L)).thenReturn(variants);

        mockMvc.perform(get("/api/products/1/variants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sku").value("SHIRT-001-RED-M"));

        verify(variantService).getVariantsByProductId(1L);
    }

    @Test
    void getVariantById_shouldReturnVariant() throws Exception {
        when(variantService.getVariantById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/variants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SHIRT-001-RED-M"))
                .andExpect(jsonPath("$.price").value(29.99));

        verify(variantService).getVariantById(1L);
    }

    @Test
    void getVariantById_shouldReturn404_whenNotFound() throws Exception {
        when(variantService.getVariantById(anyLong()))
                .thenThrow(new VariantNotFoundException("Variant not found"));

        mockMvc.perform(get("/api/variants/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVariant_shouldReturnCreatedVariant() throws Exception {
        when(variantService.createVariant(anyLong(), any(VariantRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/products/1/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SHIRT-001-RED-M"));

        verify(variantService).createVariant(anyLong(), any(VariantRequestDto.class));
    }

    @Test
    void createVariant_shouldReturn400_whenValidationFails() throws Exception {
        VariantRequestDto invalidDto = new VariantRequestDto();

        mockMvc.perform(post("/api/products/1/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVariant_shouldReturn404_whenProductNotFound() throws Exception {
        when(variantService.createVariant(anyLong(), any(VariantRequestDto.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/products/999/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateVariant_shouldReturnUpdatedVariant() throws Exception {
        when(variantService.updateVariant(anyLong(), any(VariantRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/variants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SHIRT-001-RED-M"));

        verify(variantService).updateVariant(anyLong(), any(VariantRequestDto.class));
    }

    @Test
    void updateVariant_shouldReturn404_whenNotFound() throws Exception {
        when(variantService.updateVariant(anyLong(), any(VariantRequestDto.class)))
                .thenThrow(new VariantNotFoundException("Variant not found"));

        mockMvc.perform(put("/api/variants/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteVariant_shouldReturnNoContent() throws Exception {
        doNothing().when(variantService).deleteVariant(1L);

        mockMvc.perform(delete("/api/variants/1"))
                .andExpect(status().isNoContent());

        verify(variantService).deleteVariant(1L);
    }

    @Test
    void deleteVariant_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new VariantNotFoundException("Variant not found"))
                .when(variantService).deleteVariant(anyLong());

        mockMvc.perform(delete("/api/variants/999"))
                .andExpect(status().isNotFound());
    }
}