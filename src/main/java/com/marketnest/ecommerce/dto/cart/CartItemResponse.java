package com.marketnest.ecommerce.dto.cart;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Data Transfer Object for individual cart item details in responses")
public class CartItemResponse {

    @Schema(description = "Unique identifier of the cart item", example = "1")
    private Long id;

    @Schema(description = "ID of the product in this cart item", example = "101")
    private Long productId;

    @Schema(description = "Name of the product", example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "Stock Keeping Unit of the product", example = "WH-1000XM4")
    private String productSku;

    @Schema(description = "Quantity of this product in the cart", example = "2")
    private Integer quantity;

    @Schema(description = "Current price per unit of the product", example = "299.99")
    private BigDecimal price;

    @Schema(description = "Total price for this item (quantity × price)", example = "599.98")
    private BigDecimal subtotal;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the item was added to the cart",
            example = "2025-01-15T10:30:45")
    private LocalDateTime addedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp when the item was last updated",
            example = "2025-01-15T14:20:30")
    private LocalDateTime updatedAt;
}