package com.marketnest.ecommerce.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDto {

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Compare at price must be greater than 0")
    private BigDecimal compareAtPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    private BigDecimal weight;

    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    @Size(max = 255, message = "Meta title must not exceed 255 characters")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;

    @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
    private String metaKeywords;

    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isTaxable;
}
