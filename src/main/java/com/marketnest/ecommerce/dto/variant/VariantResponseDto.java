package com.marketnest.ecommerce.dto.variant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.ProductImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object for product variant details in responses. Includes complete variant information with images")
public class VariantResponseDto {

    @Schema(description = "Unique identifier of the variant", example = "1")
    private Long id;

    @Schema(description = "ID of the product this variant belongs to", example = "101")
    private Long productId;

    @Schema(description = "Stock Keeping Unit - unique identifier for this variant",
            example = "WH-1000XM4-BLK-L")
    private String sku;

    @Schema(description = "Product barcode for scanning at checkout", example = "0012345678905",
            nullable = true)
    private String barcode;

    @Schema(description = "Name of the first variant option", example = "Color", nullable = true)
    private String option1Name;

    @Schema(description = "Value for the first variant option", example = "Black", nullable = true)
    private String option1Value;

    @Schema(description = "Name of the second variant option", example = "Size", nullable = true)
    private String option2Name;

    @Schema(description = "Value for the second variant option", example = "Large", nullable = true)
    private String option2Value;

    @Schema(description = "Name of the third variant option", example = "Edition", nullable = true)
    private String option3Name;

    @Schema(description = "Value for the third variant option", example = "Standard",
            nullable = true)
    private String option3Value;

    @Schema(
            description = "Computed display title combining all variant options (e.g., 'Black / Large / Standard')",
            example = "Black / Large",
            nullable = true
    )
    private String title;

    @Schema(description = "Selling price for this variant", example = "299.99")
    private BigDecimal price;

    @Schema(description = "Original price before discount for this variant", example = "399.99",
            nullable = true)
    private BigDecimal compareAtPrice;

    @Schema(description = "Current stock quantity available for this variant", example = "50")
    private Integer stockQuantity;

    @Schema(description = "Computed flag indicating if this variant has stock available",
            example = "true")
    private Boolean inStock;

    @Schema(description = "Variant weight in kilograms", example = "0.28", nullable = true)
    private BigDecimal weight;

    @Schema(description = "List of images specific to this variant (different from product images)")
    private List<ProductImage> images;

    @Schema(description = "Flag indicating whether this variant is available for purchase",
            example = "true")
    private Boolean isAvailable;

    @Schema(description = "Computed flag indicating if this variant is on sale (has compare-at price)",
            example = "true")
    private Boolean onSale;

    @Schema(description = "Display position/order of this variant in variant lists", example = "1",
            nullable = true)
    private Integer position;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the variant was created", example = "2025-01-15T10:30:45")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the variant was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}