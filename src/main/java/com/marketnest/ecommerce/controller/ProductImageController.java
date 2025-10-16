package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.image.ImageRequestDto;
import com.marketnest.ecommerce.model.ProductImage;
import com.marketnest.ecommerce.service.product.ProductImageService;
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
public class ProductImageController {

    private final ProductImageService productImageService;

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

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        productImageService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<?> getProductImages(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {

        return ResponseEntity.ok(productImageService.getProductImages(productId, activeOnly));
    }
}