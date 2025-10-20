package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.variant.VariantRequestDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import com.marketnest.ecommerce.service.product.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<VariantResponseDto>> getProductVariants(
            @PathVariable Long productId) {
        List<VariantResponseDto> variants = variantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(variants);
    }

    @GetMapping("/variants/{variantId}")
    public ResponseEntity<VariantResponseDto> getVariantById(@PathVariable Long variantId) {
        VariantResponseDto variant = variantService.getVariantById(variantId);
        return ResponseEntity.ok(variant);
    }

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<?> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody VariantRequestDto request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        VariantResponseDto createdVariant = variantService.createVariant(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVariant);
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<?> updateVariant(
            @PathVariable Long variantId,
            @Valid @RequestBody VariantRequestDto request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        VariantResponseDto updatedVariant = variantService.updateVariant(variantId, request);
        return ResponseEntity.ok(updatedVariant);
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
        variantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }
}