package com.marketnest.ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for individual items in an order request")
public class OrderItemRequestDto {

    @NotNull(message = "Variant ID is required")
    @Schema(
            description = "ID of the product variant being ordered (specific size, color, etc.)",
            example = "501",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long variantId;

    @NotNull(message = "Product ID is required")
    @Schema(
            description = "ID of the product being ordered",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(
            description = "Quantity of this item to order. Must be at least 1",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1"
    )
    private Integer quantity;
}