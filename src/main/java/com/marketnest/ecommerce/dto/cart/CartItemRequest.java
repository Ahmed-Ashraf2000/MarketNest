package com.marketnest.ecommerce.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for adding a product to the shopping cart")
public class CartItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(
            description = "ID of the product to add to the cart",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(
            description = "Quantity of the product to add. Must be at least 1",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer quantity;
}