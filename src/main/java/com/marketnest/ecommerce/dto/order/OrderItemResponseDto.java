package com.marketnest.ecommerce.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Data Transfer Object for individual items in an order response")
public class OrderItemResponseDto {

    @Schema(description = "Unique identifier of the order item", example = "1")
    private Long id;

    @Schema(description = "Name of the product ordered", example = "Premium Wireless Headphones")
    private String productName;

    @Schema(description = "Quantity of this item ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Price per unit at the time of order", example = "299.99")
    private BigDecimal unitPrice;

    @Schema(description = "Total price for this item (quantity × unit price)", example = "599.98")
    private BigDecimal totalPrice;
}