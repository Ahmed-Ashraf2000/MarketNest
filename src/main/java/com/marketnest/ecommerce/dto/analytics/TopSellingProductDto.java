package com.marketnest.ecommerce.dto.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for top-selling products analytics")
public class TopSellingProductDto {

    @Schema(description = "Unique identifier of the product", example = "101")
    private Long productId;

    @Schema(description = "Name of the product", example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "Stock Keeping Unit of the product", example = "WH-1000XM4")
    private String sku;

    @Schema(description = "Total quantity of this product sold across all orders", example = "145")
    private Long totalQuantitySold;

    @Schema(description = "Total revenue generated from this product", example = "43498.55")
    private BigDecimal totalRevenue;

    @Schema(description = "URL of the product's main image",
            example = "https://res.cloudinary.com/demo/image/upload/v1234567890/products/headphones.jpg",
            nullable = true)
    private String imageUrl;
}