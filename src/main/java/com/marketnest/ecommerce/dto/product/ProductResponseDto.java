package com.marketnest.ecommerce.dto.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.dto.image.ImageResponseDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for product details in responses. Includes complete product information with variants and images")
public class ProductResponseDto {

    @Schema(description = "Unique identifier of the product", example = "1")
    private Long id;

    @Schema(description = "Stock Keeping Unit - unique product identifier for inventory management",
            example = "WH-1000XM4")
    private String sku;

    @Schema(description = "Name of the product", example = "Premium Wireless Headphones")
    private String name;

    @Schema(description = "URL-friendly version of the product name",
            example = "premium-wireless-headphones")
    private String slug;

    @Schema(description = "Detailed description of the product",
            example = "High-quality wireless headphones with noise cancellation and 30-hour battery life",
            nullable = true)
    private String description;

    @Schema(description = "Current selling price of the product", example = "299.99")
    private BigDecimal price;

    @Schema(description = "Original price before discount. Shows savings to customers",
            example = "399.99", nullable = true)
    private BigDecimal compareAtPrice;

    @Schema(description = "Current stock quantity available", example = "150")
    private Integer stockQuantity;

    @Schema(description = "Threshold for low stock alerts", example = "10", nullable = true)
    private Integer lowStockThreshold;

    @Schema(description = "ID of the category this product belongs to", example = "5")
    private Long categoryId;

    @Schema(description = "Name of the category this product belongs to", example = "Electronics")
    private String categoryName;

    @Schema(description = "Brand or manufacturer name", example = "Sony", nullable = true)
    private String brand;

    @Schema(description = "Product weight in kilograms", example = "0.25", nullable = true)
    private BigDecimal weight;

    @Schema(description = "Product dimensions (L x W x H) in cm", example = "20 x 18 x 8",
            nullable = true)
    private String dimensions;

    @Schema(description = "List of product images including primary, gallery, and thumbnail images")
    private List<ImageResponseDto> images;

    @Schema(description = "List of product variants (different sizes, colors, etc.)")
    private List<VariantResponseDto> variants;

    @Schema(description = "SEO meta title for the product page",
            example = "Premium Wireless Headphones - Noise Cancelling | Sony", nullable = true)
    private String metaTitle;

    @Schema(description = "SEO meta description for the product page",
            example = "Shop premium wireless headphones with active noise cancellation",
            nullable = true)
    private String metaDescription;

    @Schema(description = "SEO meta keywords separated by commas",
            example = "wireless headphones, noise cancelling, bluetooth", nullable = true)
    private String metaKeywords;

    @Schema(description = "Flag indicating whether the product is active and visible to customers",
            example = "true")
    private Boolean isActive;

    @Schema(description = "Flag indicating whether the product is featured on homepage or promotions",
            example = "false")
    private Boolean isFeatured;

    @Schema(description = "Flag indicating whether the product is subject to tax", example = "true")
    private Boolean isTaxable;

    @Schema(description = "Computed flag indicating if product has stock available",
            example = "true")
    private Boolean inStock;

    @Schema(description = "Computed flag indicating if product stock is below threshold",
            example = "false")
    private Boolean lowStock;

    @Schema(description = "Computed flag indicating if product is on sale (has compare-at price)",
            example = "true")
    private Boolean onSale;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the product was created", example = "2025-01-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the product was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}