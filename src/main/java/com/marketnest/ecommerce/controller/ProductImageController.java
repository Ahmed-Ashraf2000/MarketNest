package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.image.ImageRequestDto;
import com.marketnest.ecommerce.model.ProductImage;
import com.marketnest.ecommerce.service.product.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "Product Image Management", description = "APIs for managing product images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Operation(summary = "Upload a product image",
            description = "Uploads a single image for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ProductImage.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long productId,
            @Valid @ModelAttribute ImageRequestDto imageRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.uploadProductImage(productId, imageRequestDto));
    }

    @Operation(summary = "Upload multiple product images",
            description = "Uploads multiple images for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Images uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ProductImage.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMultipleProductImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false, defaultValue = "GALLERY") String imageType,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive) {

        List<ImageRequestDto> requests = files.stream().map(file -> {
            ImageRequestDto request = new ImageRequestDto();
            request.setFile(file);
            request.setImageType(ProductImage.ImageType.valueOf(imageType));
            request.setIsActive(isActive);
            return request;
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.uploadMultipleProductImages(productId, requests));
    }

    @Operation(summary = "Delete a product image",
            description = "Deletes a specific image of a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        productImageService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get product images", description = "Retrieves all images for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductImage.class)))
    })
    @GetMapping
    public ResponseEntity<?> getProductImages(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {

        return ResponseEntity.ok(productImageService.getProductImages(productId, activeOnly));
    }
}