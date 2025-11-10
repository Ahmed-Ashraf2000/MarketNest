package com.marketnest.ecommerce.dto.wishlist;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marketnest.ecommerce.model.WishlistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object for individual wishlist item details in responses")
public class WishlistItemResponse {

    @Schema(description = "Unique identifier of the wishlist item", example = "1")
    private Long id;

    @Schema(description = "ID of the product in the wishlist", example = "101")
    private Long productId;

    @Schema(description = "Name of the product", example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "Stock Keeping Unit of the product", example = "WH-1000XM4")
    private String productSku;

    @Schema(description = "Current price of the product. May change over time", example = "299.99")
    private BigDecimal productPrice;

    @Schema(description = "Desired quantity of this item", example = "1")
    private Integer quantity;

    @Schema(description = "Notes about this wishlist item",
            example = "Birthday gift for mom - prefers blue color", nullable = true)
    private String notes;

    @Schema(
            description = "Priority level of this wishlist item",
            example = "HIGH",
            allowableValues = {"LOW", "MEDIUM", "HIGH"}
    )
    private WishlistItem.Priority priority;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the item was added to wishlist",
            example = "2025-01-15T10:30:45")
    private LocalDateTime addedAt;
}