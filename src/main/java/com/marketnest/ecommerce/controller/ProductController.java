package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.product.ProductRequestDto;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDto request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @Operation(summary = "Update a product", description = "Updates an existing product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                           @Valid @RequestBody ProductRequestDto request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update product status",
            description = "Updates the status of a product (active/inactive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product status updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PatchMapping("/{productId}/status")
    public ResponseEntity<?> updateProductStatus(@PathVariable Long productId,
                                                 @RequestParam Boolean isActive) {
        return ResponseEntity.ok(productService.updateProductStatus(productId, isActive));
    }

    @Operation(summary = "Get all products",
            description = "Retrieves a paginated list of products with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        return ResponseEntity.ok(productService.getAllProducts(
                categoryId, brand, minPrice, maxPrice, active, featured, search, inStock,
                pageable));
    }

    @Operation(summary = "Get product by slug", description = "Retrieves a product by its slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @Operation(summary = "Get related products",
            description = "Retrieves related products for a given product ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Related products retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}/related")
    public ResponseEntity<?> getRelatedProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "4") int limit,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(sortDirection, sort));

        return ResponseEntity.ok(
                productService.getRelatedProducts(productId, activeOnly, limit, pageable));
    }

    @Operation(summary = "Get new arrivals",
            description = "Retrieves a paginated list of new arrival products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New arrivals retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class)))
    })
    @GetMapping("/new-arrivals")
    public ResponseEntity<?> getNewArrivals(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page) {

        Sort.Direction sortDirection = Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, limit, Sort.by(sortDirection, "createdAt"));

        return ResponseEntity.ok(
                productService.getNewArrivals(activeOnly, limit, pageable));
    }

    @Operation(summary = "Get featured products",
            description = "Retrieves a paginated list of featured products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Featured products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class)))
    })
    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedProducts(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        return ResponseEntity.ok(productService.getFeaturedProducts(activeOnly, pageable));
    }
}