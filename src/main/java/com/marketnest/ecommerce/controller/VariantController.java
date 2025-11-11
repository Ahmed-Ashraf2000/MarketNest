package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.variant.VariantRequestDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import com.marketnest.ecommerce.service.product.VariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Variant Management", description = "APIs for managing product variants")
public class VariantController {

    private final VariantService variantService;

    @Operation(summary = "Get product variants",
            description = "Retrieves all variants for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variants retrieved successfully",
                    content = @Content(schema = @Schema(implementation = VariantResponseDto.class)))
    })
    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<VariantResponseDto>> getProductVariants(
            @PathVariable Long productId) {
        List<VariantResponseDto> variants = variantService.getVariantsByProductId(productId);
        return ResponseEntity.ok(variants);
    }

    @Operation(summary = "Get variant by ID",
            description = "Retrieves a specific variant by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variant retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = VariantResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    @GetMapping("/variants/{variantId}")
    public ResponseEntity<VariantResponseDto> getVariantById(@PathVariable Long variantId) {
        VariantResponseDto variant = variantService.getVariantById(variantId);
        return ResponseEntity.ok(variant);
    }

    @Operation(summary = "Create a variant",
            description = "Creates a new variant for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Variant created successfully",
                    content = @Content(
                            schema = @Schema(implementation = VariantResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Update a variant", description = "Updates an existing variant by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variant updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = VariantResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Variant not found")
    })
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

    @Operation(summary = "Delete a variant", description = "Deletes a specific variant by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Variant deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Variant not found")
    })
    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
        variantService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }
}