package com.marketnest.ecommerce.service.product;

import com.marketnest.ecommerce.dto.variant.VariantRequestDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.VariantNotFoundException;
import com.marketnest.ecommerce.mapper.Variant.VariantMapper;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.model.ProductVariant;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.VariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariantServiceTest {

    @Mock
    private VariantRepository variantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private VariantMapper variantMapper;

    @InjectMocks
    private VariantService variantService;

    private Product testProduct;
    private ProductVariant testVariant;
    private VariantRequestDto requestDto;
    private VariantResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("T-Shirt");

        testVariant = new ProductVariant();
        testVariant.setId(1L);
        testVariant.setProductId(1L);
        testVariant.setSku("SHIRT-001-RED-M");
        testVariant.setOption1Name("Color");
        testVariant.setOption1Value("Red");
        testVariant.setOption2Name("Size");
        testVariant.setOption2Value("M");
        testVariant.setPrice(new BigDecimal("29.99"));
        testVariant.setStockQuantity(50);

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
        responseDto.setPrice(new BigDecimal("29.99"));
    }

    @Test
    void getVariantsByProductId_shouldReturnVariants_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(variantRepository.findByProductId(1L))
                .thenReturn(Collections.singletonList(testVariant));
        when(variantMapper.toResponseList(any())).thenReturn(
                Collections.singletonList(responseDto));

        List<VariantResponseDto> result = variantService.getVariantsByProductId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSku()).isEqualTo("SHIRT-001-RED-M");
        verify(productRepository).findById(1L);
        verify(variantRepository).findByProductId(1L);
    }

    @Test
    void getVariantsByProductId_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.getVariantsByProductId(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");
    }

    @Test
    void getVariantById_shouldReturnVariant_whenExists() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(testVariant));
        when(variantMapper.toResponse(testVariant)).thenReturn(responseDto);

        VariantResponseDto result = variantService.getVariantById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(variantRepository).findById(1L);
    }

    @Test
    void getVariantById_shouldThrowException_whenNotFound() {
        when(variantRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.getVariantById(999L))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessageContaining("Variant not found with ID: 999");
    }

    @Test
    void createVariant_shouldCreateAndReturnVariant_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(variantMapper.toEntity(requestDto)).thenReturn(testVariant);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(testVariant);
        when(variantMapper.toResponse(testVariant)).thenReturn(responseDto);

        VariantResponseDto result = variantService.createVariant(1L, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).findById(1L);
        verify(variantRepository).save(any(ProductVariant.class));
    }

    @Test
    void createVariant_shouldThrowException_whenProductNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.createVariant(999L, requestDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");
    }

    @Test
    void updateVariant_shouldUpdateAndReturnVariant_whenExists() {
        when(variantRepository.findById(1L)).thenReturn(Optional.of(testVariant));
        when(variantMapper.toEntity(requestDto)).thenReturn(testVariant);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(testVariant);
        when(variantMapper.toResponse(testVariant)).thenReturn(responseDto);

        VariantResponseDto result = variantService.updateVariant(1L, requestDto);

        assertThat(result).isNotNull();
        verify(variantRepository).findById(1L);
        verify(variantRepository).save(any(ProductVariant.class));
    }

    @Test
    void updateVariant_shouldThrowException_whenNotFound() {
        when(variantRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> variantService.updateVariant(999L, requestDto))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessageContaining("Variant not found with ID: 999");
    }

    @Test
    void deleteVariant_shouldDeleteVariant_whenExists() {
        when(variantRepository.existsById(1L)).thenReturn(true);

        variantService.deleteVariant(1L);

        verify(variantRepository).existsById(1L);
        verify(variantRepository).deleteById(1L);
    }

    @Test
    void deleteVariant_shouldThrowException_whenNotFound() {
        when(variantRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> variantService.deleteVariant(999L))
                .isInstanceOf(VariantNotFoundException.class)
                .hasMessageContaining("Variant not found with ID: 999");
    }
}