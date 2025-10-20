package com.marketnest.ecommerce.service.product;

import com.marketnest.ecommerce.dto.variant.VariantRequestDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.exception.VariantNotFoundException;
import com.marketnest.ecommerce.mapper.Variant.VariantMapper;
import com.marketnest.ecommerce.model.ProductVariant;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantService {

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final VariantMapper variantMapper;

    @Transactional(readOnly = true)
    public List<VariantResponseDto> getVariantsByProductId(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        return variantMapper.toResponseList(variants);
    }

    @Transactional(readOnly = true)
    public VariantResponseDto getVariantById(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException(
                        "Variant not found with ID: " + variantId));
        return variantMapper.toResponse(variant);
    }

    @Transactional
    public VariantResponseDto createVariant(Long productId, VariantRequestDto request) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        ProductVariant variant = variantMapper.toEntity(request);
        variant.setProductId(productId);

        ProductVariant savedVariant = variantRepository.save(variant);
        return variantMapper.toResponse(savedVariant);
    }

    @Transactional
    public VariantResponseDto updateVariant(Long variantId, VariantRequestDto request) {
        ProductVariant existingVariant = variantRepository.findById(variantId)
                .orElseThrow(() -> new VariantNotFoundException(
                        "Variant not found with ID: " + variantId));

        ProductVariant updatedVariant = variantMapper.toEntity(request);
        updatedVariant.setId(existingVariant.getId());
        updatedVariant.setProductId(existingVariant.getProductId());
        updatedVariant.setCreatedAt(existingVariant.getCreatedAt());

        ProductVariant savedVariant = variantRepository.save(updatedVariant);
        return variantMapper.toResponse(savedVariant);
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new VariantNotFoundException("Variant not found with ID: " + variantId);
        }
        variantRepository.deleteById(variantId);
    }
}