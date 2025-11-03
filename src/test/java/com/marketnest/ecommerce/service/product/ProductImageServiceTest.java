package com.marketnest.ecommerce.service.product;

import com.marketnest.ecommerce.dto.image.ImageRequestDto;
import com.marketnest.ecommerce.dto.image.ImageResponseDto;
import com.marketnest.ecommerce.exception.ProductImageNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.mapper.image.ImageMapper;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.ProductImage;
import com.marketnest.ecommerce.repository.ProductImageRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProductImageService productImageService;

    private Product testProduct;
    private ProductImage testImage;
    private ImageRequestDto requestDto;
    private ImageResponseDto responseDto;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testImage = new ProductImage();
        testImage.setId(1L);
        testImage.setProductId(1L);
        testImage.setUrl("https://example.com/image.jpg");
        testImage.setFileName("image.jpg");
        testImage.setFileSize(1024L);
        testImage.setMimeType("image/jpeg");
        testImage.setImageType(ProductImage.ImageType.MAIN);
        testImage.setIsActive(true);

        mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        requestDto = new ImageRequestDto();
        requestDto.setFile(mockFile);
        requestDto.setImageType(ProductImage.ImageType.MAIN);
        requestDto.setIsActive(true);

        responseDto = new ImageResponseDto();
        responseDto.setId(1L);
        responseDto.setUrl("https://example.com/image.jpg");
        responseDto.setImageType(ProductImage.ImageType.MAIN);
    }

    @Test
    void uploadProductImage_shouldUploadAndReturnImage_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cloudinaryService.uploadProductImage(mockFile))
                .thenReturn("https://example.com/uploaded.jpg");
        when(imageMapper.toEntity(requestDto)).thenReturn(testImage);
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(testImage);
        when(imageMapper.toResponse(testImage)).thenReturn(responseDto);

        ImageResponseDto result = productImageService.uploadProductImage(1L, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).findById(1L);
        verify(cloudinaryService).uploadProductImage(mockFile);
        verify(productImageRepository).save(any(ProductImage.class));
    }

    @Test
    void uploadProductImage_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.uploadProductImage(999L, requestDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");

        verify(cloudinaryService, never()).uploadProductImage(any());
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void uploadProductImage_shouldSetImageMetadata() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cloudinaryService.uploadProductImage(mockFile))
                .thenReturn("https://example.com/uploaded.jpg");
        when(imageMapper.toEntity(requestDto)).thenReturn(testImage);
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(testImage);
        when(imageMapper.toResponse(testImage)).thenReturn(responseDto);

        productImageService.uploadProductImage(1L, requestDto);

        verify(productImageRepository).save(argThat(image ->
                image.getProductId().equals(1L) &&
                image.getUrl().equals("https://example.com/uploaded.jpg") &&
                image.getFileName().equals("test-image.jpg") &&
                image.getFileSize().equals((long) mockFile.getSize()) &&
                image.getMimeType().equals("image/jpeg")
        ));
    }

    @Test
    void uploadMultipleProductImages_shouldUploadAllImages() {
        ImageRequestDto request1 = new ImageRequestDto();
        request1.setFile(mockFile);
        request1.setImageType(ProductImage.ImageType.MAIN);

        ImageRequestDto request2 = new ImageRequestDto();
        request2.setFile(mockFile);
        request2.setImageType(ProductImage.ImageType.GALLERY);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cloudinaryService.uploadProductImage(any()))
                .thenReturn("https://example.com/image1.jpg")
                .thenReturn("https://example.com/image2.jpg");
        when(imageMapper.toEntity(any())).thenReturn(testImage);
        when(productImageRepository.save(any())).thenReturn(testImage);
        when(imageMapper.toResponse(any())).thenReturn(responseDto);

        List<ImageResponseDto> results = productImageService.uploadMultipleProductImages(
                1L, Arrays.asList(request1, request2));

        assertThat(results).hasSize(2);
        verify(cloudinaryService, times(2)).uploadProductImage(any());
        verify(productImageRepository, times(2)).save(any());
    }

    @Test
    void deleteProductImage_shouldDeleteImage_whenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        productImageService.deleteProductImage(1L, 1L);

        verify(productRepository).findById(1L);
        verify(productImageRepository).findById(1L);
        verify(productImageRepository).delete(testImage);
    }

    @Test
    void deleteProductImage_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.deleteProductImage(999L, 1L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void deleteProductImage_shouldThrowException_whenImageNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.deleteProductImage(1L, 999L))
                .isInstanceOf(ProductImageNotFoundException.class)
                .hasMessageContaining("Product image not found with ID: 999");

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void deleteProductImage_shouldThrowException_whenImageBelongsToDifferentProduct() {
        testImage.setProductId(2L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        assertThatThrownBy(() -> productImageService.deleteProductImage(1L, 1L))
                .isInstanceOf(ProductImageNotFoundException.class)
                .hasMessageContaining("Image does not belong to product with ID: 1");

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void getProductImages_shouldReturnActiveImages_whenActiveOnlyTrue() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findByProductIdAndIsActiveTrue(1L))
                .thenReturn(Collections.singletonList(testImage));
        when(imageMapper.toResponseList(any())).thenReturn(Collections.singletonList(responseDto));

        List<ImageResponseDto> results = productImageService.getProductImages(1L, true);

        assertThat(results).hasSize(1);
        verify(productImageRepository).findByProductIdAndIsActiveTrue(1L);
        verify(productImageRepository, never()).findByProductId(anyLong());
    }

    @Test
    void getProductImages_shouldReturnAllImages_whenActiveOnlyFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findByProductId(1L))
                .thenReturn(Collections.singletonList(testImage));
        when(imageMapper.toResponseList(any())).thenReturn(Collections.singletonList(responseDto));

        List<ImageResponseDto> results = productImageService.getProductImages(1L, false);

        assertThat(results).hasSize(1);
        verify(productImageRepository).findByProductId(1L);
        verify(productImageRepository, never()).findByProductIdAndIsActiveTrue(anyLong());
    }

    @Test
    void getProductImages_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.getProductImages(999L, true))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");

        verify(productImageRepository, never()).findByProductId(anyLong());
        verify(productImageRepository, never()).findByProductIdAndIsActiveTrue(anyLong());
    }

    @Test
    void getProductImages_shouldReturnEmptyList_whenNoImages() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productImageRepository.findByProductIdAndIsActiveTrue(1L))
                .thenReturn(List.of());
        when(imageMapper.toResponseList(any())).thenReturn(List.of());

        List<ImageResponseDto> results = productImageService.getProductImages(1L, true);

        assertThat(results).isEmpty();
    }
}