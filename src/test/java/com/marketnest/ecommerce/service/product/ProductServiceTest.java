package com.marketnest.ecommerce.service.product;

import com.marketnest.ecommerce.dto.product.ProductRequestDto;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.mapper.product.ProductMapper;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.repository.CategoryRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequestDto productRequestDto;
    private ProductResponseDto productResponseDto;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = new Product();
        product.setId(1L);
        product.setSku("SKU001");
        product.setName("Laptop");
        product.setSlug("laptop");
        product.setPrice(new BigDecimal("999.99"));
        product.setCategoryId(1L);
        product.setIsActive(true);

        productRequestDto = new ProductRequestDto();
        productRequestDto.setSku("SKU001");
        productRequestDto.setName("Laptop");
        productRequestDto.setPrice(new BigDecimal("999.99"));
        productRequestDto.setCategoryId(1L);

        productResponseDto = new ProductResponseDto();
        productResponseDto.setId(1L);
        productResponseDto.setName("Laptop");
        productResponseDto.setPrice(new BigDecimal("999.99"));
    }

    @Test
    void testCreateProduct() {
        when(productMapper.toEntity(any(ProductRequestDto.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        ProductResponseDto result = productService.createProduct(productRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toEntity(any(ProductRequestDto.class));
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        ProductResponseDto result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findById(1L);
        verify(productMapper, times(1)).toResponse(any(Product.class));
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 1");

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Laptop");

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productMapper.toEntity(any(ProductRequestDto.class))).thenReturn(updatedProduct);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        ProductResponseDto result = productService.updateProduct(1L, productRequestDto);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(1L, productRequestDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 1");

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 1");

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateProductStatus() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        ProductResponseDto result = productService.updateProductStatus(1L, false);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetProductsByCategoryId() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryIdAndIsActiveTrue(anyLong(), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        Page<ProductResponseDto> result =
                productService.getProductsByCategoryId(1L, true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByCategoryIdAndIsActiveTrue(anyLong(),
                any(Pageable.class));
    }

    @Test
    void testGetProductsByCategoryId_CategoryNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductsByCategoryId(1L, true, pageable))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with ID: 1");

        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductBySlug() {
        when(productRepository.findBySlug(anyString())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        ProductResponseDto result = productService.getProductBySlug("laptop");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findBySlug("laptop");
    }

    @Test
    void testGetProductBySlug_NotFound() {
        when(productRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductBySlug("nonexistent"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with slug: nonexistent");

        verify(productRepository, times(1)).findBySlug("nonexistent");
    }

    @Test
    void testGetFeaturedProducts() {
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByIsFeaturedTrueAndIsActiveTrue(any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponseDto);

        Page<ProductResponseDto> result = productService.getFeaturedProducts(true, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository, times(1)).findByIsFeaturedTrueAndIsActiveTrue(
                any(Pageable.class));
    }
}