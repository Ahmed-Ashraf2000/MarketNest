package com.marketnest.ecommerce.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Data Transfer Object for creating or updating products")
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    @Schema(
            description = "Name of the product. Must be unique and between 2-255 characters",
            example = "Premium Wireless Headphones",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 255
    )
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(
            description = "Detailed description of the product, features, and specifications",
            example = "High-quality wireless headphones with noise cancellation and 30-hour battery life",
            maxLength = 2000,
            nullable = true
    )
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2,
            message = "Price must have at most 10 digits and 2 decimal places")
    @Schema(
            description = "Selling price of the product. Must be greater than 0",
            example = "299.99",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "Compare-at price must be greater than 0")
    @Digits(integer = 10, fraction = 2,
            message = "Compare-at price must have at most 10 digits and 2 decimal places")
    @Schema(
            description = "Original price before discount (for showing savings). Must be greater than selling price",
            example = "399.99",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal compareAtPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(
            description = "Current stock quantity available. Cannot be negative",
            example = "150",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0"
    )
    private Integer stockQuantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    @Schema(
            description = "Threshold for low stock alerts. Triggers notification when stock falls below this number",
            example = "10",
            minimum = "0",
            nullable = true
    )
    private Integer lowStockThreshold;

    @NotNull(message = "Category ID is required")
    @Schema(
            description = "ID of the category this product belongs to",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long categoryId;

    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    @Schema(
            description = "Brand or manufacturer name",
            example = "Sony",
            maxLength = 100,
            nullable = true
    )
    private String brand;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @Schema(
            description = "Product weight in kilograms. Used for shipping calculations",
            example = "0.25",
            minimum = "0",
            nullable = true
    )
    private BigDecimal weight;

    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    @Schema(
            description = "Product dimensions (L x W x H) in cm. Used for shipping calculations",
            example = "20 x 18 x 8",
            maxLength = 100,
            nullable = true
    )
    private String dimensions;

    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    @Schema(
            description = "SEO meta title for the product page",
            example = "Premium Wireless Headphones - Noise Cancelling | Sony",
            maxLength = 255,
            nullable = true
    )
    private String metaTitle;

    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    @Schema(
            description = "SEO meta description for the product page",
            example = "Shop premium wireless headphones with active noise cancellation, 30-hour battery, and superior sound quality",
            maxLength = 500,
            nullable = true
    )
    private String metaDescription;

    @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
    @Schema(
            description = "SEO meta keywords separated by commas",
            example = "wireless headphones, noise cancelling, bluetooth headphones, sony headphones",
            maxLength = 500,
            nullable = true
    )
    private String metaKeywords;

    @Schema(
            description = "Flag indicating whether the product is active and visible to customers",
            example = "true",
            nullable = true
    )
    private Boolean isActive;

    @Schema(
            description = "Flag indicating whether the product should be featured on homepage or promotions",
            example = "false",
            nullable = true
    )
    private Boolean isFeatured;

    @Schema(
            description = "Flag indicating whether the product is subject to tax",
            example = "true",
            nullable = true
    )
    private Boolean isTaxable;
}