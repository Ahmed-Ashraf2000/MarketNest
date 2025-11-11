package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.image.ImageRequestDto;
import com.marketnest.ecommerce.dto.image.ImageResponseDto;
import com.marketnest.ecommerce.exception.ProductImageNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.model.ProductImage;
import com.marketnest.ecommerce.service.product.ProductImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductImageController.class)
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductImageService productImageService;

    private ImageResponseDto responseDto;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        responseDto = new ImageResponseDto();
        responseDto.setId(1L);
        responseDto.setUrl("https://example.com/image.jpg");
        responseDto.setImageType(ProductImage.ImageType.MAIN);
        responseDto.setIsActive(true);

        mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    void uploadProductImage_shouldReturnCreatedImage() throws Exception {
        when(productImageService.uploadProductImage(anyLong(), any(ImageRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/api/products/1/images")
                        .file(mockFile)
                        .param("imageType", "MAIN")
                        .param("isActive", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.url").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.imageType").value("MAIN"));

        verify(productImageService).uploadProductImage(anyLong(), any(ImageRequestDto.class));
    }

    @Test
    void uploadProductImage_shouldReturn404_whenProductNotFound() throws Exception {
        when(productImageService.uploadProductImage(anyLong(), any(ImageRequestDto.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(multipart("/api/products/999/images")
                        .file(mockFile)
                        .param("imageType", "MAIN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadProductImage_shouldReturn400_whenFileIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/products/1/images")
                        .param("imageType", "MAIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadMultipleProductImages_shouldReturnCreatedImages() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "image1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "image2.jpg", "image/jpeg", "content2".getBytes());

        List<ImageResponseDto> responses = Arrays.asList(responseDto, responseDto);
        when(productImageService.uploadMultipleProductImages(anyLong(), anyList()))
                .thenReturn(responses);

        mockMvc.perform(multipart("/api/products/1/images/batch")
                        .file(file1)
                        .file(file2)
                        .param("imageType", "GALLERY")
                        .param("isActive", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(productImageService).uploadMultipleProductImages(anyLong(), anyList());
    }

    @Test
    void uploadMultipleProductImages_shouldUseDefaultValues() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "image.jpg", "image/jpeg", "content".getBytes());

        when(productImageService.uploadMultipleProductImages(anyLong(), anyList()))
                .thenReturn(Collections.singletonList(responseDto));

        mockMvc.perform(multipart("/api/products/1/images/batch")
                        .file(file))
                .andExpect(status().isCreated());

        verify(productImageService).uploadMultipleProductImages(anyLong(), anyList());
    }

    @Test
    void deleteProductImage_shouldReturnNoContent() throws Exception {
        doNothing().when(productImageService).deleteProductImage(1L, 1L);

        mockMvc.perform(delete("/api/products/1/images/1"))
                .andExpect(status().isNoContent());

        verify(productImageService).deleteProductImage(1L, 1L);
    }

    @Test
    void deleteProductImage_shouldReturn404_whenImageNotFound() throws Exception {
        doThrow(new ProductImageNotFoundException("Image not found"))
                .when(productImageService).deleteProductImage(anyLong(), anyLong());

        mockMvc.perform(delete("/api/products/1/images/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProductImage_shouldReturn404_whenProductNotFound() throws Exception {
        doThrow(new ProductNotFoundException("Product not found"))
                .when(productImageService).deleteProductImage(anyLong(), anyLong());

        mockMvc.perform(delete("/api/products/999/images/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductImages_shouldReturnImages_withActiveOnlyTrue() throws Exception {
        List<ImageResponseDto> images = Collections.singletonList(responseDto);
        when(productImageService.getProductImages(1L, true)).thenReturn(images);

        mockMvc.perform(get("/api/products/1/images")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].url").value("https://example.com/image.jpg"));

        verify(productImageService).getProductImages(1L, true);
    }

    @Test
    void getProductImages_shouldReturnImages_withActiveOnlyFalse() throws Exception {
        List<ImageResponseDto> images = Collections.singletonList(responseDto);
        when(productImageService.getProductImages(1L, false)).thenReturn(images);

        mockMvc.perform(get("/api/products/1/images")
                        .param("activeOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(productImageService).getProductImages(1L, false);
    }

    @Test
    void getProductImages_shouldUseDefaultActiveOnly() throws Exception {
        when(productImageService.getProductImages(1L, true))
                .thenReturn(Collections.singletonList(responseDto));

        mockMvc.perform(get("/api/products/1/images"))
                .andExpect(status().isOk());

        verify(productImageService).getProductImages(1L, true);
    }

    @Test
    void getProductImages_shouldReturn404_whenProductNotFound() throws Exception {
        when(productImageService.getProductImages(anyLong(), anyBoolean()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/999/images"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductImages_shouldReturnEmptyList_whenNoImages() throws Exception {
        when(productImageService.getProductImages(1L, true))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/products/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}