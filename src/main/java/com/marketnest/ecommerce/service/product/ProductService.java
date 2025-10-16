package com.marketnest.ecommerce.service.product;

import com.marketnest.ecommerce.dto.product.ProductRequestDto;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.mapper.product.ProductMapper;
import com.marketnest.ecommerce.model.Product;
import com.marketnest.ecommerce.repository.CategoryRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto request) {
        Product product = productMapper.toEntity(request);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto request) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        Product updatedProduct = productMapper.toEntity(request);
        updatedProduct.setId(existingProduct.getId());
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());

        Product savedProduct = productRepository.save(updatedProduct);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
    }

    @Transactional
    public ProductResponseDto updateProductStatus(Long productId, Boolean isActive) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        product.setIsActive(isActive);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategoryId(Long categoryId, boolean activeOnly,
                                                            Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with ID: " + categoryId));

        Page<Product> productPage;
        if (activeOnly) {
            productPage = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        } else {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        }

        return productPage.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(
            Long categoryId,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isActive,
            Boolean isFeatured,
            String search,
            Boolean inStock,
            Pageable pageable) {

        Page<Product> productPage;

        if (search != null && !search.isEmpty()) {
            if (isActive != null && isActive) {
                productPage =
                        productRepository.findByIsActiveTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                search, search, pageable);
            } else {
                productPage =
                        productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                search, search, pageable);
            }
        } else if (isActive != null && isActive) {
            productPage = productRepository.findByIsActiveTrue(pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }


        List<Product> filteredContent = productPage.getContent().stream()
                .filter(product -> categoryId == null || product.getCategoryId().equals(categoryId))
                .filter(product -> brand == null || (product.getBrand() != null && product.getBrand()
                        .equalsIgnoreCase(brand)))
                .filter(product -> minPrice == null || (product.getPrice() != null && product.getPrice()
                                                                                              .compareTo(
                                                                                                      minPrice) >= 0))
                .filter(product -> maxPrice == null || (product.getPrice() != null && product.getPrice()
                                                                                              .compareTo(
                                                                                                      maxPrice) <= 0))
                .filter(product -> isFeatured == null || product.getIsFeatured().equals(isFeatured))
                .filter(product -> inStock == null || product.isInStock() == inStock)
                .collect(Collectors.toList());


        filteredContent.forEach(product -> {
            if (product.getCategoryId() != null) {
                product.setCategory(
                        categoryRepository.findById(product.getCategoryId()).orElse(null));
            }
        });


        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), filteredContent.size());

        Page<Product> filteredPage = new PageImpl<>(
                filteredContent.subList(start, end),
                pageable,
                filteredContent.size());

        return filteredPage.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(
                        () -> new ProductNotFoundException("Product not found with slug: " + slug));

        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getRelatedProducts(Long productId, boolean activeOnly,
                                                       int limit, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        if (product.getCategoryId() == null) {
            return Page.empty(pageable);
        }

        Page<Product> relatedProducts;
        if (activeOnly) {
            relatedProducts = productRepository.findByCategoryIdAndIsActiveTrueAndIdNot(
                    product.getCategoryId(), product.getId(),
                    PageRequest.of(0, limit, pageable.getSort()));
        } else {
            relatedProducts = productRepository.findByCategoryIdAndIdNot(
                    product.getCategoryId(), product.getId(),
                    PageRequest.of(0, limit, pageable.getSort()));
        }

        relatedProducts.getContent().forEach(relatedProduct -> {
            if (relatedProduct.getCategoryId() != null) {
                relatedProduct.setCategory(
                        categoryRepository.findById(relatedProduct.getCategoryId()).orElse(null));
            }
        });

        return relatedProducts.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getNewArrivals(boolean activeOnly, int limit,
                                                   Pageable pageable) {
        Page<Product> newArrivals;

        if (activeOnly) {
            newArrivals = productRepository.findByIsActiveTrueOrderByCreatedAtDesc(
                    PageRequest.of(0, limit, pageable.getSort()));
        } else {
            newArrivals = productRepository.findByOrderByCreatedAtDesc(
                    PageRequest.of(0, limit, pageable.getSort()));
        }

        return newArrivals.map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getFeaturedProducts(boolean activeOnly, Pageable pageable) {
        Page<Product> productPage;

        if (activeOnly) {
            productPage = productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable);
        } else {
            productPage = productRepository.findByIsFeaturedTrue(pageable);
        }

        return productPage.map(productMapper::toResponse);
    }
}