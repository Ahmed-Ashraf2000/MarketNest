package com.marketnest.ecommerce.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for products with low stock levels that require replenishment")
public class LowStockProductDto {

    @Schema(description = "Unique identifier of the product", example = "101")
    private Long productId;

    @Schema(description = "Name of the product", example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "Stock Keeping Unit of the product", example = "WH-1000XM4")
    private String sku;

    @Schema(description = "Current available stock quantity for the product", example = "5")
    private Integer currentStock;

    @Schema(description = "Threshold value below which the product is considered low stock",
            example = "10")
    private Integer lowStockThreshold;

    @Schema(description = "URL of the product's main image",
            example = "https://res.cloudinary.com/demo/image/upload/v1234567890/products/headphones.jpg",
            nullable = true)
    private String imageUrl;
}