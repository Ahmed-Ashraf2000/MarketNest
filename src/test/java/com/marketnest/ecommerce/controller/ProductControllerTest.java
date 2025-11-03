package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.product.ProductRequestDto;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductRequestDto productRequestDto;
    private ProductResponseDto productResponseDto;

    @BeforeEach
    void setUp() {
        productRequestDto = new ProductRequestDto();
        productRequestDto.setSku("SKU001");
        productRequestDto.setName("Laptop");
        productRequestDto.setSlug("laptop");
        productRequestDto.setPrice(new BigDecimal("999.99"));
        productRequestDto.setCategoryId(1L);

        productResponseDto = new ProductResponseDto();
        productResponseDto.setId(1L);
        productResponseDto.setName("Laptop");
        productResponseDto.setSlug("laptop");
        productResponseDto.setPrice(new BigDecimal("999.99"));
    }

    @Test
    void testCreateProduct() throws Exception {
        when(productService.createProduct(any(ProductRequestDto.class)))
                .thenReturn(productResponseDto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService, times(1)).createProduct(any(ProductRequestDto.class));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById(anyLong())).thenReturn(productResponseDto);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void testUpdateProduct() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequestDto.class)))
                .thenReturn(productResponseDto);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService, times(1)).updateProduct(anyLong(), any(ProductRequestDto.class));
    }

    @Test
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(anyLong());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void testUpdateProductStatus() throws Exception {
        when(productService.updateProductStatus(anyLong(), anyBoolean()))
                .thenReturn(productResponseDto);

        mockMvc.perform(patch("/api/products/1/status")
                        .param("isActive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService, times(1)).updateProductStatus(1L, false);
    }

    @Test
    void testGetAllProducts() throws Exception {
        Page<ProductResponseDto> productPage = new PageImpl<>(
                Collections.singletonList(productResponseDto));
        when(productService.getAllProducts(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));

        verify(productService, times(1)).getAllProducts(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetProductBySlug() throws Exception {
        when(productService.getProductBySlug(anyString())).thenReturn(productResponseDto);

        mockMvc.perform(get("/api/products/slug/laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("laptop"));

        verify(productService, times(1)).getProductBySlug("laptop");
    }

    @Test
    void testGetRelatedProducts() throws Exception {
        Page<ProductResponseDto> productPage = new PageImpl<>(
                Collections.singletonList(productResponseDto));
        when(productService.getRelatedProducts(anyLong(), anyBoolean(), anyInt(), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/products/1/related"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));

        verify(productService, times(1)).getRelatedProducts(anyLong(), anyBoolean(), anyInt(),
                any());
    }

    @Test
    void testGetNewArrivals() throws Exception {
        Page<ProductResponseDto> productPage = new PageImpl<>(
                Collections.singletonList(productResponseDto));
        when(productService.getNewArrivals(anyBoolean(), anyInt(), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/products/new-arrivals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));

        verify(productService, times(1)).getNewArrivals(anyBoolean(), anyInt(), any());
    }

    @Test
    void testGetFeaturedProducts() throws Exception {
        Page<ProductResponseDto> productPage = new PageImpl<>(
                Collections.singletonList(productResponseDto));
        when(productService.getFeaturedProducts(anyBoolean(), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));

        verify(productService, times(1)).getFeaturedProducts(anyBoolean(), any());
    }
}