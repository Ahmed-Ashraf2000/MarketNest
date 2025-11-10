package com.marketnest.ecommerce.dto.variant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for creating or updating product variants (different sizes, colors, etc.)")
public class VariantRequestDto {

    @NotBlank(message = "Variant SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Schema(
            description = "Stock Keeping Unit - unique identifier for this variant in inventory management",
            example = "WH-1000XM4-BLK-L",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    private String sku;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    @Schema(
            description = "Product barcode (UPC, EAN, ISBN, etc.) for scanning at checkout",
            example = "0012345678905",
            maxLength = 100,
            nullable = true
    )
    private String barcode;

    @Size(max = 50, message = "Option1 name must not exceed 50 characters")
    @Schema(
            description = "Name of the first variant option (e.g., 'Color', 'Size', 'Material')",
            example = "Color",
            maxLength = 50,
            nullable = true
    )
    private String option1Name;

    @Size(max = 100, message = "Option1 value must not exceed 100 characters")
    @Schema(
            description = "Value for the first variant option (e.g., 'Black', 'Large', 'Cotton')",
            example = "Black",
            maxLength = 100,
            nullable = true
    )
    private String option1Value;

    @Size(max = 50, message = "Option2 name must not exceed 50 characters")
    @Schema(
            description = "Name of the second variant option (e.g., 'Size', 'Storage', 'Style')",
            example = "Size",
            maxLength = 50,
            nullable = true
    )
    private String option2Name;

    @Size(max = 100, message = "Option2 value must not exceed 100 characters")
    @Schema(
            description = "Value for the second variant option (e.g., 'Medium', '256GB', 'Slim')",
            example = "Large",
            maxLength = 100,
            nullable = true
    )
    private String option2Value;

    @Size(max = 50, message = "Option3 name must not exceed 50 characters")
    @Schema(
            description = "Name of the third variant option (e.g., 'Pattern', 'Finish', 'Edition')",
            example = "Edition",
            maxLength = 50,
            nullable = true
    )
    private String option3Name;

    @Size(max = 100, message = "Option3 value must not exceed 100 characters")
    @Schema(
            description = "Value for the third variant option (e.g., 'Striped', 'Matte', 'Limited')",
            example = "Standard",
            maxLength = 100,
            nullable = true
    )
    private String option3Value;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have max 8 digits and 2 decimals")
    @Schema(
            description = "Selling price for this specific variant. Must be greater than 0",
            example = "299.99",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Compare at price must be greater than 0")
    @Digits(integer = 8, fraction = 2,
            message = "Compare at price must have max 8 digits and 2 decimals")
    @Schema(
            description = "Original price before discount for this variant. Shows savings to customers",
            example = "399.99",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal compareAtPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(
            description = "Current stock quantity available for this variant. Cannot be negative",
            example = "50",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0"
    )
    private Integer stockQuantity;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Weight must have max 8 digits and 2 decimals")
    @Schema(
            description = "Variant weight in kilograms. Used for shipping calculations if different from base product",
            example = "0.28",
            minimum = "0.01",
            nullable = true
    )
    private BigDecimal weight;

    @Schema(
            description = "Flag indicating whether this variant is available for purchase",
            example = "true",
            nullable = true
    )
    private Boolean isAvailable;

    @Min(value = 0, message = "Position cannot be negative")
    @Schema(
            description = "Display position/order of this variant. Lower numbers appear first in variant lists",
            example = "1",
            minimum = "0",
            nullable = true
    )
    private Integer position;
}